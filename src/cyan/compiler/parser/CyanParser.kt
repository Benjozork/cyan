package cyan.compiler.parser

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken

interface Item

data class VariableDeclaration(val name: String) : Item

class VariableListParser : Grammar<List<VariableDeclaration>>() {

    val let   by literalToken("let")
    val ws    by regexToken("\\s+")
    val ident by regexToken("\\w+")
    val comma by regexToken(",\\s+")

    private val variableParser by (let and ws and ident).map { (_, _, name) -> VariableDeclaration(name.text) }

    override val rootParser by separatedTerms(variableParser, comma)

}
