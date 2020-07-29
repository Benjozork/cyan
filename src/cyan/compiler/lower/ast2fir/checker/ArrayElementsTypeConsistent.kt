package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.CyanArrayExpression

object ArrayElementsTypeConsistent : Check<FirExpression> {

    override fun check(firNode: FirExpression, containingNode: FirNode): Boolean {
        if (firNode.astExpr !is CyanArrayExpression) return false

        val arrayValueTypeSet = firNode.astExpr.exprs.map { firNode.makeChildExpr(it).type() }.toSet()

        return arrayValueTypeSet.size > 1
    }


}
