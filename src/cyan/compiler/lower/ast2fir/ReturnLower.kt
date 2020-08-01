package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReturn
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanReturn

object ReturnLower : Ast2FirLower<CyanReturn, FirReturn> {

    override fun lower(astNode: CyanReturn, parentFirNode: FirNode): FirReturn {
        val containingFunction = (parentFirNode.parent as? FirFunctionDeclaration)
            ?: DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "return is not allowed here",
                    astNode = astNode
                )
            )

        val firReturn = FirReturn(parentFirNode)
        val firReturnExpr = ExpressionLower.lower(astNode.expr, firReturn)

        if (!(containingFunction.returnType accepts firReturnExpr.type())) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type mismatch: expected '${containingFunction.returnType}', found '${firReturnExpr.type()}'",
                    astNode = astNode
                )
            )
        }

        firReturn.expr = firReturnExpr

        return firReturn
    }

}
