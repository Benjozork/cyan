package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.parser.ast.types.CyanStructDeclaration
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

object StructDeclarationLower : Ast2FirLower<CyanStructDeclaration, FirTypeDeclaration> {

    override fun lower(astNode: CyanStructDeclaration, parentFirNode: FirNode): FirTypeDeclaration {
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

        val structType = Type.Struct(astNode.ident.value, astNode.properties.map {
            if (it.type !is CyanTypeAnnotation.Literal) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "struct properties cannot only have primitive types",
                        astNode = it
                    )
                )
            }

            Type.Struct.Property(it.ident.value, it.type.literalType)
        }.toTypedArray())

        val firStructDeclaration = FirTypeDeclaration(parentFirNode, astNode.ident.value, structType)

        parentFirNode.declaredSymbols += firStructDeclaration

        return firStructDeclaration
    }

}
