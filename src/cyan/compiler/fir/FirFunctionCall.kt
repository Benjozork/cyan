package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirFunctionCall(override val parent: FirNode, val callee: FirSymbol, val args: Array<FirExpression>) : FirStatement {

    override fun allReferredSymbols() = setOf(callee, *args.flatMap(FirExpression::allReferredSymbols).toTypedArray()).toSet()

}
