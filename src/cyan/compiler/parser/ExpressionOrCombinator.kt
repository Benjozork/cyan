package cyan.compiler.parser

import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.parser.*

class ExpressionOrCombinator<T>(vararg val parsers: Parser<T>, val forbiddenTokens: List<Token>) : Parser<T> {
    override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> {
        var failures: ArrayList<ErrorResult>? = null
        loop@ for (element in parsers) {
            if (forbiddenTokens.any { it.tryParse(tokens, fromPosition) !is ErrorResult })
                return AlternativesFailure(failures.orEmpty())
            when (val result = element.tryParse(tokens, fromPosition)) {
                is Parsed -> return result
                is ErrorResult -> {
                    if (result is UnexpectedEof) break@loop
                    if (failures == null)
                        failures = ArrayList()
                    failures.add(result)
                }
            }
        }
        return AlternativesFailure(failures.orEmpty())
    }
}
