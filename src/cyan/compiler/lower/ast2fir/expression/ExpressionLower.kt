package cyan.compiler.lower.ast2fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.lower.ast2fir.Ast2FirLower
import cyan.compiler.parser.ast.expression.CyanExpression

object ExpressionLower : Ast2FirLower<CyanExpression, FirExpression> {

    override fun lower(astNode: CyanExpression, parentFirNode: FirNode): FirExpression {
        return FirExpression(parentFirNode, astNode)
    }

}
