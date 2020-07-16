package cyan.compiler.lower.ast2fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.lower.ast2fir.Ast2FirLower
import cyan.compiler.lower.ast2fir.checker.ArrayElementsTypeConsistent
import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanExpression

object ExpressionLower : Ast2FirLower<CyanExpression, FirExpression> {

    override fun lower(astNode: CyanExpression, parentFirNode: FirNode): FirExpression {
        val firExpression = FirExpression(parentFirNode, astNode)

        if (firExpression.astExpr is CyanArrayExpression) {
            require (!ArrayElementsTypeConsistent.check(firExpression, parentFirNode)) { "heterogeneous arrays are not allowed" }
        }

        return firExpression
    }

}
