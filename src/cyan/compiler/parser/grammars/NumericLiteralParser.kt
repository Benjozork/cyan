package cyan.compiler.parser.grammars

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.utils.Tuple2

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.util.span

class NumericLiteralParser : Grammar<CyanNumericLiteralExpression>() {

    private fun Iterable<String>.squish() = joinToString("")

    val minus by literalToken("-")

    val separator by literalToken("_")

    val binaryBase  by literalToken("0b")
    val hexBase     by literalToken("0x")
    val decimalBase by literalToken("0d")

    val hexCharacter     by regexToken("(?i)[abcdef]")
    val decimalCharacter by regexToken("[23456789]")
    val binaryCharacter  by regexToken("[01]")

    val binaryDigit  by binaryCharacter
    val decimalDigit by binaryCharacter or decimalCharacter
    val hexDigit     by binaryCharacter or decimalCharacter or hexCharacter

    val binarySequence  by oneOrMore(binaryDigit)  use { this.map { it.text }.squish() to span(this) }
    val decimalSequence by oneOrMore(decimalDigit) use { this.map { it.text }.squish() to span(this) }
    val hexSequence     by oneOrMore(hexDigit)     use { this.map { it.text }.squish() to span(this) }

    private fun Tuple2<TokenMatch?, List<Pair<String, Span>>>.makeNumberSpan() =
            t1?.let { span(t1!!, t2.last().second) } ?: span(t2.first().second, t2.last().second)

    val binaryNumber by (optional(minus) * -binaryBase * separatedTerms(binarySequence, separator, false))
            .use { listOfNotNull(t1?.let { "-" }, *t2.map { it.first }.toTypedArray()).squish().toInt(2) to makeNumberSpan() }

    val decimalNumber by (optional(minus) * -optional(decimalBase) * separatedTerms(decimalSequence, separator, false))
            .use { listOfNotNull(t1?.let { "-" }, *t2.map { it.first }.toTypedArray()).squish().toInt(10) to makeNumberSpan() }

    val hexNumber by (optional(minus) * -hexBase * separatedTerms(hexSequence, separator, false))
            .use { listOfNotNull(t1?.let { "-" }, *t2.map { it.first }.toTypedArray()).squish().toInt(16) to makeNumberSpan() }

    override val rootParser = (binaryNumber or decimalNumber or hexNumber).use { CyanNumericLiteralExpression(first, second) }

}
