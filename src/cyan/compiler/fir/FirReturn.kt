package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirReturn(override val parent: FirNode) : FirStatement {

    lateinit var expr: FirExpression

    override fun toString() = "return $expr"

    override fun allReferredSymbols() = expr.allReferredSymbols()

}
