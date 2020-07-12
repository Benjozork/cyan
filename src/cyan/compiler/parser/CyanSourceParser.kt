package cyan.compiler.parser

import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionSignature
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.ast.operator.CyanBinaryPlusOperator

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
    val function        by literalToken("function")

    val assign          by literalToken("=")

    val leap            by literalToken("(")
    val reap            by literalToken(")")

    val lcur            by literalToken("{")
    val rcur            by literalToken("}")

    val lsq             by literalToken("[")
    val rsq             by literalToken("]")

    val dot             by literalToken(".")

    val comma           by literalToken(",")

    // Misc.

    val commaParser    by comma and ws

    // Arithmetic

    val plus            by literalToken("+")
    val minus           by literalToken("-")

    // Values

    val ident           by regexToken("[a-zA-Z]+")
    val numericalValue  by regexToken("\\d+")

    val stringLiteral   by regexToken("\".*?\"")

    // Value parsers

    val referenceParser      by ident          use { CyanIdentifierExpression(text) }
    val stringLiteralParser  by stringLiteral  use { CyanStringLiteralExpression(text.removeSurrounding("\"")) }
    val numericalValueParser by numericalValue use { CyanNumericLiteralExpression(text.toInt()) }

    // Operators

    val plusParser  by plus use { CyanBinaryPlusOperator }
    val minusParser by minus use { CyanBinaryMinusOperator }

    val operator by (plusParser or minusParser)

    // Members

    val memberAccessParser by (referenceParser * -dot * referenceParser) use { CyanMemberAccessExpression(t1, t2) }

    // Expressions

    val literalExpressionParser: Parser<CyanExpression>    by (numericalValueParser or stringLiteralParser)

    val arrayExpressionParser: Parser<CyanArrayExpression> by (-lsq * separatedTerms(parser(this::expressionParser), commaParser, true) * -rsq)
            .use { CyanArrayExpression(this.toTypedArray()) }

    val binaryExpressionParser by (literalExpressionParser * -ws * operator * -ws * literalExpressionParser) use { CyanBinaryExpression(t1, t2, t3) }

    val expressionParser: Parser<CyanExpression> by
    arrayExpressionParser or binaryExpressionParser or literalExpressionParser or memberAccessParser or referenceParser

    // Functions

    val functionSignature by (-function * -ws * referenceParser * -optional(ws) * -leap * separatedTerms(referenceParser, commaParser, true) * -reap)
            .use { CyanFunctionSignature(t1, t2) }

    val functionBody by (-lcur * -optional(newLine) * parser(this::rootParser) * -optional(newLine) * -rcur)

    val functionDeclaration: Parser<CyanFunctionDeclaration> by (functionSignature * -ws * functionBody)
            .use { CyanFunctionDeclaration(t1, t2) }

    // Statements

    val variableIdentification by (-let * -ws * referenceParser)
    val variableInitialization by (-ws * -assign * -ws * expressionParser)

    val variableDeclaration    by (variableIdentification and variableInitialization)                                     use { CyanVariableDeclaration(t1, t2) }
    val functionCall           by (referenceParser * -leap * separatedTerms(expressionParser, commaParser, true) * -reap) use { CyanFunctionCall(t1, t2.toTypedArray()) }
    val statement              by -optional(ws) * (variableDeclaration or functionDeclaration or functionCall) * -optional(ws)

    // Root parser

    val sourceParser = separatedTerms(statement, newLine) use { CyanSource(this) }

    override val rootParser = sourceParser

}
