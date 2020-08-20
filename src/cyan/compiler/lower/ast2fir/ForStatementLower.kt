package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirForStatement
import cyan.compiler.fir.FirNode
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanForStatement

object ForStatementLower : Ast2FirLower<CyanForStatement, FirForStatement> {

    override fun lower(astNode: CyanForStatement, parentFirNode: FirNode): FirForStatement {
        val loweredIteratorExpr = ExpressionLower.lower(astNode.iteratorExpr, parentFirNode)

        if (!loweredIteratorExpr.type().array) DiagnosticPipe.report (
            CompilerDiagnostic (
                level = CompilerDiagnostic.Level.Error,
                message = "Type mismatch: expected 'any[]', found '${loweredIteratorExpr.type()}'",
                astNode = astNode, span = astNode.iteratorExpr.span
            )
        )

        val firForStatement = FirForStatement(parentFirNode, loweredIteratorExpr)

        // Add iterator variable to declared symbols
        firForStatement.declaredSymbols += FirForStatement.IteratorVariable(firForStatement, astNode.variableName.value, loweredIteratorExpr.type())

        val loweredBody = InheritingSourceLower.lower(astNode.source, firForStatement)

        firForStatement.block = loweredBody

        loweredIteratorExpr.parent = firForStatement

        return firForStatement
    }

}
