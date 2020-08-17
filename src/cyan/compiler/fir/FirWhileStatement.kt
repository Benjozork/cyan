package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirWhileStatement (
    override val parent: FirNode,
    val conditionExpr: FirExpression,
    var block: FirSource? = null
) : FirStatement {

    override fun allReferredSymbols() = conditionExpr.allReferredSymbols() + (block?.allReferredSymbols() ?: emptySet())

}
