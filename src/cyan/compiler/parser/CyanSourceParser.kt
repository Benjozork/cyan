package cyan.compiler.parser

import cyan.compiler.parser.items.*
import cyan.compiler.parser.items.function.CyanFunctionCall
import cyan.compiler.parser.items.function.CyanFunctionDeclaration
import cyan.compiler.parser.items.function.CyanFunctionSignature
import cyan.compiler.parser.items.expression.CyanIdentifierExpression
import cyan.compiler.parser.items.expression.CyanBinaryExpression
import cyan.compiler.parser.items.expression.CyanExpression
import cyan.compiler.parser.items.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.items.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.items.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.items.operator.CyanBinaryPlusOperator

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

@Suppress("MemberVisibilityCanBePrivate")
class CyanSourceParser : Grammar<CyanSource>() {

    // Tokens

    val newLine         by regexToken("\n|\r\n")
    val ws              by regexToken("\\s+")

    val let             by literalToken("let")

    val assign          by literalToken("=")

    val leap            by literalToken("(")
    val reap            by literalToken(")")

    val lcur            by literalToken("{")
    val rcur            by literalToken("}")

    val comma           by literalToken(",") and ws

    val squot           by literalToken("'")
    val dquot           by literalToken("\"")

    // Arithmetic

    val plus            by literalToken("+")
    val minus           by literalToken("-")

    val ident           by regexToken("[a-zA-Z]+")
    val numericalValue  by regexToken("\\d+")

    // Value parsers

    val referenceParser      by ident                                                                      use { CyanIdentifierExpression(text) }
    val stringLiteralParser  by (-squot * referenceParser * -squot) or (-dquot * referenceParser * -dquot) use { CyanStringLiteralExpression(value) }
    val numericalValueParser by numericalValue                                                             use { CyanNumericLiteralExpression(text.toInt()) }

    // Operators

    val plusParser  by plus use { CyanBinaryPlusOperator }
    val minusParser by minus use { CyanBinaryMinusOperator }

    val operator by (plusParser or minusParser)

    // Expressions

    val literalExpressionParser: Parser<CyanExpression> by (numericalValueParser or stringLiteralParser)

    val binaryExpressionParser by (literalExpressionParser * -ws * operator * -ws * literalExpressionParser) use { CyanBinaryExpression(t1, t2, t3) }

    val expressionParser: Parser<CyanExpression> by (binaryExpressionParser or literalExpressionParser or referenceParser)

    // Functions

    val functionSignature by (referenceParser * -optional(ws) * -leap * separatedTerms(referenceParser, comma, true) * -reap)
        .use { CyanFunctionSignature(t1, t2) }

    val functionBody      by (-lcur * -optional(newLine) * parser(this::rootParser) * -optional(newLine) * -rcur)

    val functionDeclaration: Parser<CyanFunctionDeclaration> by (functionSignature * -ws * functionBody)
        .use { CyanFunctionDeclaration(t1, t2) }

    // Statements

    val variableIdentification by (-let * -ws * referenceParser)
    val variableInitialization by (-ws * -assign * -ws * expressionParser)

    val variableDeclaration by (variableIdentification and optional(variableInitialization))
        .use { CyanVariableDeclaration(t1, t2) }

    val functionCall by (referenceParser * -leap * separatedTerms(expressionParser, comma, true) * -reap)
        .map { (name, args) -> CyanFunctionCall(name, args.toTypedArray()) }

    val statement = -optional(ws) * (variableDeclaration or functionDeclaration or functionCall) * -optional(ws)

    // Root parser

    val sourceParser = separatedTerms(statement, newLine) use { CyanSource(this) }

    override val rootParser = sourceParser

}
