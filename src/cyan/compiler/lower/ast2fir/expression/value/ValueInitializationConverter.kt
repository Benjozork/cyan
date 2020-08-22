package cyan.compiler.lower.ast2fir.expression.value

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

object ValueInitializationConverter {

    fun convert(astInitializer: CyanFunctionCall, parentFirNode: FirNode): FirExpression.Literal.Struct {
        require (astInitializer.base is CyanIdentifierExpression)

        val typeReference = FirReference(parentFirNode, astInitializer.base.value, astInitializer.base)
        val resolvedTypeSymbol = parentFirNode.findSymbol(typeReference)?.resolvedSymbol ?: DiagnosticPipe.report (
            CompilerDiagnostic (
                level = CompilerDiagnostic.Level.Error,
                message = "Unresolved type '${typeReference.text}'",
                astNode = astInitializer, span = astInitializer.base.span
            )
        )

        require (resolvedTypeSymbol is FirTypeDeclaration.Struct)

        // First, check there are no mixes of named and positional arguments

        val argumentKindsValid = astInitializer.args.map { it.label == null }.toSet().size == 1
        if (!argumentKindsValid) DiagnosticPipe.report (
            CompilerDiagnostic (
                level = CompilerDiagnostic.Level.Error,
                message = "Mixing named and positional arguments is not allowed",
                astNode = astInitializer
            )
        )

        val positional = astInitializer.args.first().label == null

        val structTypeProperties = resolvedTypeSymbol.type.properties

        val resolvedFields = if (positional) {
            val values = (structTypeProperties zip astInitializer.args).toMap()

            if (values.size < structTypeProperties.size) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Not enough arguments for initialization of struct",
                    astNode = astInitializer
                )
            )

            if (values.size > structTypeProperties.size) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Too many arguments for initialization of struct",
                    astNode = astInitializer
                )
            )

            values
        } else {
            val values = structTypeProperties.toList().associateWith { property ->
                astInitializer.args.firstOrNull { argument -> argument.label!!.value == property.name }
                        ?: DiagnosticPipe.report (
                            CompilerDiagnostic (
                                level = CompilerDiagnostic.Level.Error,
                                message = "Missing value for struct property '${property.name}'",
                                astNode = astInitializer
                            )
                        )
            }

            if (values.size < astInitializer.args.size) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Unknown struct property values in initializer",
                    astNode = astInitializer
                )
            )

            values
        }

        return FirExpression.Literal.Struct (
            elements = resolvedFields.mapValues { ExpressionLower.lower(it.value.value, parentFirNode) },
            type = resolvedTypeSymbol.type,
            parent = parentFirNode,
            fromAstNode = astInitializer
        ).also { e -> e.elements.values.forEach { it.parent = e } }
    }

}
