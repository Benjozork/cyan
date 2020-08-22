package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Derive
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.parser.ast.CyanDerive
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionReceiver
import cyan.compiler.parser.ast.function.CyanFunctionSignature
import cyan.compiler.parser.ast.types.CyanStructDeclaration
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

object StructDeclarationLower : Ast2FirLower<CyanStructDeclaration, FirTypeDeclaration.Struct> {

    override fun lower(astNode: CyanStructDeclaration, parentFirNode: FirNode): FirTypeDeclaration.Struct {
        if (astNode.properties.map { it.ident.value }.toSet().size != astNode.properties.size) { // Check for duplicate properties
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "A struct cannot have multiple properties with the same name",
                    astNode = astNode
                )
            )
        }

        astNode.properties
            .firstOrNull { it.type is CyanTypeAnnotation.Literal && it.type.literalType == Type.Primitive(CyanType.Void, false) }?.let { property -> // Check for void properties
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "struct property cannot have void type",
                        astNode = property
                    )
                )
            }

        if (parentFirNode !is FirScope) { // Check parent is scope
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "FirVariableDeclaration found in ${parentFirNode::class.simpleName}",
                    astNode = astNode
                )
            )
        }

        val loweredStructProperties = astNode.properties.map {
            if (it.type !is CyanTypeAnnotation.Literal) {
                DiagnosticPipe.report(
                    CompilerDiagnostic(
                        level = CompilerDiagnostic.Level.Error,
                        message = "struct properties cannot only have primitive types",
                        astNode = it
                    )
                )
            }

            Type.Struct.Property(it.ident.value, it.type.literalType)
        }.toTypedArray()

        val structType = Type.Struct(astNode.ident.value, loweredStructProperties, mutableListOf())

        val loweredSelfDerives = astNode.derives.map { derive ->
            val traitReference = FirReference(parentFirNode, derive.traitAnnotation.identifierExpression.value, derive.traitAnnotation.identifierExpression)
            val resolvedTraitSymbol = parentFirNode.findSymbol(traitReference)
                ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Unresolved trait '${traitReference.text}'",
                        astNode = derive, span = derive.traitAnnotation.span
                    )
                )

            val symbol = resolvedTraitSymbol.resolvedSymbol

            if (symbol !is FirTypeDeclaration<*>) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Symbol '${traitReference.text}' is not a trait",
                    astNode = derive, span = derive.traitAnnotation.span
                )
            )

            if (symbol.type !is Type.Trait) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type '${traitReference.text}' is not a trait",
                    astNode = derive, span = derive.traitAnnotation.span
                )
            )

            val traitSymbol = symbol.type as Type.Trait

            val firDeriveNode = Derive(parentFirNode, traitSymbol)
            firDeriveNode.onType = structType

            val loweredFunctionImpls = traitSymbol.elements.filterIsInstance<Type.Trait.Element.Function>().map { traitFunction ->
                val matchingDeriveImpl = derive.impls.filterIsInstance<CyanDerive.Item.Function>().firstOrNull { it.name.value == traitFunction. name }
                    ?: DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "Missing implementation for function '${traitFunction.name}' of trait '${traitSymbol.name}'",
                            astNode = derive, span = derive.traitAnnotation.span
                        )
                    )

                val fakeAstFunctionDeclaration = CyanFunctionDeclaration (
                    CyanFunctionSignature (
                        attributes = emptyList(),
                        receiver = CyanFunctionReceiver(CyanTypeAnnotation.Reference(CyanIdentifierExpression("self"))),
                        name = matchingDeriveImpl.name,
                        args = matchingDeriveImpl.args.toList(),
                        typeAnnotation = matchingDeriveImpl.returnType,
                        isExtern = false,
                        span = matchingDeriveImpl.span
                    ),
                    matchingDeriveImpl.body
                )

                if (!(traitFunction.returnType accepts parentFirNode.resolveType(matchingDeriveImpl.returnType))) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Function '${traitFunction.name}' of trait '${traitSymbol.name}' specifies return type '${traitFunction.returnType}', which does not accept type '${matchingDeriveImpl.returnType}'",
                        astNode = matchingDeriveImpl, span = matchingDeriveImpl.returnType.span
                    )
                )

                traitFunction to FunctionDeclarationLower.lower(fakeAstFunctionDeclaration, firDeriveNode)
            }.toMap()

            firDeriveNode.functionImpls = loweredFunctionImpls
            firDeriveNode.propertyImpls = emptyMap()

            derive.impls.filter { it.name.value !in traitSymbol.elements.filterIsInstance<Type.Trait.Element.Function>().map { e -> e.name } }.forEach { badImpl ->
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Trait '${traitSymbol.name}' defines no function called '${badImpl.name}'",
                        astNode = derive, span = badImpl.name.span
                    )
                )
            }

            firDeriveNode
        }.toTypedArray()

        val firStructDeclaration = FirTypeDeclaration.Struct(parentFirNode, structType, loweredSelfDerives)

        loweredSelfDerives.forEach { it.parent = firStructDeclaration }
        loweredSelfDerives.forEach { it.onType = structType }

        structType.derives += loweredSelfDerives

        parentFirNode.declaredSymbols += firStructDeclaration

        return firStructDeclaration
    }

}
