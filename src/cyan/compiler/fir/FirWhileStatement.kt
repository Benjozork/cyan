package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirWhileStatement (
    override val parent: FirNode,
    val conditionExpr: FirExpression,
    val block: FirSource.Inheriting
) : FirStatement, FirScope by block {

    override fun allReferredSymbols() = conditionExpr.allReferredSymbols() + block.allReferredSymbols()

}
