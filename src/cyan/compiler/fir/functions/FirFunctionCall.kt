package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.expression.FirExpression

class FirFunctionCall(override val parent: FirNode, override var args: Array<FirExpression> = emptyArray()) : FirStatement, FirCall {

    override lateinit var callee: FirResolvedReference

    override fun allReferredSymbols() = setOf(callee, *args.flatMap(FirExpression::allReferredSymbols).toTypedArray()).toSet()
}
