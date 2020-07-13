package cyan.compiler.parser

import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionSignature
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.*

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
    val times           by literalToken("*")
    val div             by literalToken("/")
    val mod             by literalToken("%")
    val exp             by literalToken("^")

    val tokenToOp = mapOf (
        plus  to CyanBinaryPlusOperator,
        minus to CyanBinaryMinusOperator,
        times to CyanBinaryTimesOperator,
        div   to CyanBinaryModOperator,
        mod   to CyanBinaryModOperator,
        exp   to CyanBinaryExpOperator
    )

    // Comparison

    val equ             by literalToken("==")
    val neq             by literalToken("!=")
    val lt              by literalToken("<")
    val leq             by literalToken("<=")
    val gt              by literalToken(">")
    val geq             by literalToken(">=")

    // Boolean

    val and             by literalToken("&&")
    val or              by literalToken("||")

    val trueToken       by literalToken("true")
    val falseToken      by literalToken("false")

    // Values

    val ident           by regexToken("[a-zA-Z]+")
    val numericalValue  by regexToken("\\d+")

    val stringLiteral   by regexToken("\".*?\"")

    // Value parsers

    val referenceParser      by ident                     use { CyanIdentifierExpression(text) }
    val stringLiteralParser  by stringLiteral             use { CyanStringLiteralExpression(text.removeSurrounding("\"")) }
    val numericalValueParser by numericalValue            use { CyanNumericLiteralExpression(text.toInt()) }
    val booleanLiteralParser by (trueToken or falseToken) use { CyanBooleanLiteralExpression(type == trueToken) }

    // Operators

    val plusParser  by plus use { CyanBinaryModOperator }
    val minusParser by minus use { CyanBinaryMinusOperator }

    val operator by (plusParser or minusParser)

    // Members

    val memberAccessParser by (referenceParser * -dot * referenceParser) use { CyanMemberAccessExpression(t1, t2) }

    // Expressions

    val literalExpressionParser by (numericalValueParser or stringLiteralParser or booleanLiteralParser)

    val arrayExpressionParser by (-lsq * separatedTerms(parser(this::expr), commaParser, true) * -rsq)
            .use { CyanArrayExpression(this.toTypedArray()) }

    val parenTerm = (-leap * parser(this::expr) * -reap)

    val term by (parenTerm or arrayExpressionParser or literalExpressionParser or memberAccessParser or referenceParser)

    val mulDivModOp by (times or div or mod) use { tokenToOp[this.type]!! }
    val mulDivModOrTerm: Parser<CyanExpression> by leftAssociative(term, -optional(ws) * mulDivModOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r) }

    val plusMinusOp by (plus or minus) use { tokenToOp[this.type]!! }
    val arithmetic: Parser<CyanExpression> by leftAssociative(mulDivModOrTerm, -optional(ws) * plusMinusOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r) }

    val comparisonOp by equ or neq or lt or leq or gt or geq
    val comparisonOrMath: Parser<CyanExpression> by (arithmetic * optional(comparisonOp * arithmetic))
            .map { (left, tail) -> tail?.let { (op, r) -> CyanBinaryExpression(left, tokenToOp[op.type]!!, r) } ?: left }

    val andChain by leftAssociative(comparisonOrMath, and) { l, _, r -> CyanBinaryExpression(l, CyanBinaryAndOperator, r) }
    val orChain  by leftAssociative(andChain, and) { l, _, r -> CyanBinaryExpression(l, CyanBinaryOrOperator, r) }

    val expr by orChain

    // Functions

    val functionSignature by (-function * -ws * referenceParser * -optional(ws) * -leap * separatedTerms(referenceParser, commaParser, true) * -reap)
            .use { CyanFunctionSignature(t1, t2) }

    val functionBody by (-lcur * -optional(newLine) * parser(this::rootParser) * -optional(newLine) * -rcur)

    val functionDeclaration: Parser<CyanFunctionDeclaration> by (functionSignature * -ws * functionBody)
            .use { CyanFunctionDeclaration(t1, t2) }

    // Statements

    val variableIdentification by (-let * -ws * referenceParser)
    val variableInitialization by (-ws * -assign * -ws * expr)

    val variableDeclaration    by (variableIdentification and variableInitialization)                                     use { CyanVariableDeclaration(t1, t2) }
    val functionCall           by (referenceParser * -leap * separatedTerms(expr, commaParser, true) * -reap) use { CyanFunctionCall(t1, t2.toTypedArray()) }
    val statement              by -optional(ws) * (variableDeclaration or functionDeclaration or functionCall) * -optional(ws)

    // Root parser

    val sourceParser = separatedTerms(statement, newLine) use { CyanSource(this) }

    override val rootParser = sourceParser

}
