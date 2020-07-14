package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirFunctionCall(val callee: String, val args: Array<FirExpression>) : FirStatement
