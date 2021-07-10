package cyan.compiler.parser.util

import cyan.compiler.parser.ast.expression.CyanArrayIndexExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.CyanMemberAccessExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

object DotChainAssociator {

    fun associate(dotChain: List<CyanExpression>): CyanExpression {
        if (dotChain.size == 1) return dotChain.first()

        val acc = dotChain.last()
        val term = dotChain[dotChain.lastIndex - 1]

        return when (acc) {
            is CyanIdentifierExpression -> {
                val base = associate(dotChain.dropLast(1))

                CyanMemberAccessExpression(base, acc, span(base, acc))
            }
            is CyanArrayIndexExpression -> {
                val base = associate(dotChain.dropLast(1) + acc.base)

                CyanArrayIndexExpression(base, acc.index, span(base, acc))
            }
            is CyanFunctionCall -> {
                val base = associate(dotChain.dropLast(1))

                CyanFunctionCall(CyanMemberAccessExpression(base, acc.base as CyanIdentifierExpression), acc.args, span(term, acc))
            }
            else -> error("cannot associate with right-hand side '${acc::class.simpleName}'")
        }
    }

}
