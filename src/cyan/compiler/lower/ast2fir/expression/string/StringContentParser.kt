package cyan.compiler.lower.ast2fir.expression.string

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken

class StringContentParser : Grammar<StringContent>() {

    val backslash by literalToken("\\")

    val n by regexToken("n\\b")
    val t by regexToken("t\\b")

    val textCharacters by regexToken("[^\\\\]*")

    val specialCharactersMap: Map<Token, String> = mapOf (
        n to "\n",
        t to "\t"
    )

    val specialCharacter by -backslash * (n or t) use { StringContent.Escape(specialCharactersMap[this.type] ?: error("invalid escape: '$text'")) }

    val escapedBackslash by -backslash * backslash use { StringContent.Escape("\\") }

    val anyEscape by specialCharacter or escapedBackslash

    val text by textCharacters use { StringContent.Text(this.text) }

    val content by anyEscape or text

    override val rootParser = zeroOrMore(content) use { StringContent(this) }

}
