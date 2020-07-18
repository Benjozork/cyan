package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanType
import cyan.compiler.parser.ast.CyanVariableDeclaration

object VariableDeclarationLower : Ast2FirLower<CyanVariableDeclaration, FirVariableDeclaration> {

    override fun lower(astNode: CyanVariableDeclaration, parentFirNode: FirNode): FirVariableDeclaration {
        val typeAnnotation = astNode.type?.let { Type(it.base, it.array) }

        val firVariableDeclaration = FirVariableDeclaration (
            parent = parentFirNode,
            name = astNode.name.value,
            typeAnnotation = typeAnnotation,
            initializationExpr = ExpressionLower.lower(astNode.value, parentFirNode)
        )

        if (parentFirNode !is FirScope) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    astNode = astNode,
                    message = "FirVariableDeclaration found in ${parentFirNode::class.simpleName}"
                )
            )
        }

        if (typeAnnotation?.base == CyanType.Void) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    astNode = astNode,
                    message = "Variables cannot have 'void' type"
                )
            )
        }

        if (typeAnnotation != null && !(typeAnnotation accepts firVariableDeclaration.initializationExpr.type())) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    astNode = astNode,
                    message = "Type mismatch: expected '${firVariableDeclaration.typeAnnotation}', found '${firVariableDeclaration.initializationExpr.type()}'"
                )
            )
        }

        parentFirNode.declaredSymbols += firVariableDeclaration

        return firVariableDeclaration
    }

}
