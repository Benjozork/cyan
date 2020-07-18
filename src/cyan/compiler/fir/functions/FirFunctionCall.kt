package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.FirSymbol
import cyan.compiler.fir.expression.FirExpression

class FirFunctionCall(override val parent: FirNode, val callee: FirSymbol, var args: Array<FirExpression>) : FirStatement {

    override fun allReferredSymbols() = setOf(callee, *args.flatMap(FirExpression::allReferredSymbols).toTypedArray()).toSet()

}
