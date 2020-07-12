package cyan.compiler.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken

interface Item

data class VariableDeclaration(val name: String, val value: Int?) : Item

@Suppress("MemberVisibilityCanBePrivate")
class CyanSourceParser : Grammar<List<VariableDeclaration>>() {

    val newLine         by regexToken("\n|\r\n")
    val ws              by regexToken("\\s+")
    val let             by literalToken("let")
    val ident           by regexToken("[a-zA-Z]+")
    val assign          by literalToken("=")
    val numericalValue  by regexToken("\\d+")

    val variableIdentification by (-let * -ws * ident)                   use { text }
    val variableInitialization by (-ws * -assign * -ws * numericalValue) use { text.toInt() }

    val variableDeclaration by (-optional(ws) * variableIdentification and optional(variableInitialization) * -optional(ws))
        .use { VariableDeclaration(t1, t2) }

    val sourceParser = separatedTerms(variableDeclaration, newLine)

    override val rootParser = sourceParser

}
