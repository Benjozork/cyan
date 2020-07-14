package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirFunctionCall
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.function.CyanFunctionCall

object FunctionCallLower : Ast2FirLower<CyanFunctionCall, FirFunctionCall> {

    override fun lower(astNode: CyanFunctionCall): FirFunctionCall {
        return FirFunctionCall(astNode.functionIdentifier.value, astNode.args.map { ExpressionLower.lower(it) }.toTypedArray())
    }


}
