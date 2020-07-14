package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirFunctionCall(val callee: String, val args: Array<FirExpression>) : FirStatement {

    override fun allReferences() = setOf(callee, *args.flatMap(FirExpression::allReferences).toTypedArray()).toSet()

}
