package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirExpressionStatement(override val parent: FirNode, val expr: FirExpression) : FirStatement {

    override fun allReferredSymbols() = expr.allReferredSymbols()

}
