package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanWhileStatement
import cyan.compiler.fir.FirWhileStatement

object WhileStatementLower : Ast2FirLower<CyanWhileStatement, FirWhileStatement> {

    override fun lower(astNode: CyanWhileStatement, parentFirNode: FirNode): FirWhileStatement {
        val loweredExpr = ExpressionLower.lower(astNode.conditionExpr, parentFirNode)

        if (!(Type.Primitive(CyanType.Bool, false) accepts loweredExpr.type())) DiagnosticPipe.report (
            CompilerDiagnostic (
                level = CompilerDiagnostic.Level.Error,
                message = "while statement condition must be a boolean expression, not '${loweredExpr.type()}'",
                astNode = astNode, span = astNode.conditionExpr.span
            )
        )

        val firWhileStatement = FirWhileStatement(parentFirNode, loweredExpr)

        val loweredSource = InheritingSourceLower.lower(astNode.source, firWhileStatement)

        firWhileStatement.block = loweredSource

        loweredExpr.parent = firWhileStatement
        loweredSource.parent = firWhileStatement

        return firWhileStatement
    }

}
