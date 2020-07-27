package cyan.compiler.parser

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.types.CyanStructDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionSignature
import cyan.compiler.parser.ast.function.CyanFunctionArgument
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
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

@Suppress("MemberVisibilityCanBePrivate")
class CyanSourceParser : Grammar<CyanSource>() {

    // Tokens

    val newLine         by regexToken("\n|\r\n")
    val ws              by regexToken("\\s+")

    val let             by literalToken("let")
    val vark            by literalToken("var")
    val extern          by literalToken("extern")
    val function        by literalToken("function")
    val ifToken         by literalToken("if")
    val elseToken       by literalToken("else")

    // Complex types

    val type            by literalToken("type")
    val struct          by literalToken("struct")

    // Types

    val anyPrim         by literalToken("any")
    val voidPrim        by literalToken("void")
    val int8Prim        by literalToken("i8")
    val int32Prim       by literalToken("i32")
    val int64Prim       by literalToken("i64")
    val float32Prim     by literalToken("f32")
    val float64Prim     by literalToken("f64")
    val boolPrim        by literalToken("bool")
    val strPrim         by literalToken("str")
    val charPrim        by literalToken("char")

    val tokenToType = mapOf (
        anyPrim     to CyanType.Any,
        voidPrim    to CyanType.Void,
        int8Prim    to CyanType.Int8,
        int32Prim   to CyanType.Int32,
        int64Prim   to CyanType.Int64,
        float32Prim to CyanType.Float32,
        float64Prim to CyanType.Float64,
        boolPrim    to CyanType.Bool,
        strPrim     to CyanType.Str,
        charPrim    to CyanType.Char
    )

    val arraySuffix     by literalToken("[]")

    val assign          by literalToken("=")

    val leap            by literalToken("(")
    val reap            by literalToken(")")

    val lcur            by literalToken("{")
    val rcur            by literalToken("}")

    val lsq             by literalToken("[")
    val rsq             by literalToken("]")

    val colon           by literalToken(":")

    val dot             by literalToken(".")

    val comma           by literalToken(",")

    // Misc. base parsers

    val znws               by zeroOrMore(newLine or ws) // Zero or more Newlines or WhiteSpaces
    val commaParser        by comma and znws

    // Type base parsers

    val primTypeName  by (anyPrim or voidPrim or int8Prim or int32Prim or int64Prim or float32Prim or float64Prim or boolPrim or strPrim or charPrim)
    val litType      by (primTypeName * optional(arraySuffix)) use { CyanTypeAnnotation.Literal(Type.Primitive(tokenToType[t1.type]!!, t2 != null)) }
    val refType       by (parser(this::referenceParser))        use { CyanTypeAnnotation.Reference(this) }
    val typeSignature by (-optional(colon) * -znws * (litType or refType))

    // Complex type parsers

    val structProperty    by (parser(this::referenceParser) * typeSignature) use { CyanStructDeclaration.Property(t1, t2) }
    val structProperties  by separatedTerms(structProperty, commaParser)     use { this.toTypedArray() }
    val structBody        by (-lcur * -znws * structProperties * -znws * -rcur)
    val structDeclaration by (-struct * -znws * structBody)

    val complexType by (structDeclaration)

    val typeDeclaration by (-type * -znws * parser(this::referenceParser) * -znws * -assign * -znws * complexType) use { CyanStructDeclaration(t1, t2) }

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

    val deq             by literalToken("==")
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

    // Members

    val memberAccessParser by (referenceParser * -dot * referenceParser)                                   use { CyanMemberAccessExpression(t1, t2) }
    val arrayIndexParser   by (parser(this::unambiguousTerm) * -znws * -lsq * numericalValueParser * -rsq) use { CyanArrayIndexExpression(t1, t2) }

    // Expressions

    val literalExpressionParser by (numericalValueParser or stringLiteralParser or booleanLiteralParser)

    val arrayExpressionParser by (-lsq * separatedTerms(parser(this::expr), commaParser, true) * -rsq)
            .use { CyanArrayExpression(this.toTypedArray()) }

    val parenTerm = (-leap * parser(this::expr) * -reap)

    val unambiguousTerm: Parser<CyanExpression> by (parenTerm or memberAccessParser or referenceParser)
    val term: Parser<CyanExpression>            by (parenTerm or arrayExpressionParser or literalExpressionParser or memberAccessParser or arrayIndexParser or referenceParser)

    val mulDivModOp by (times or div or mod) use { tokenToOp[this.type]!! }
    val mulDivModOrTerm: Parser<CyanExpression> by leftAssociative(term, -optional(ws) * mulDivModOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r) }

    val plusMinusOp by (plus or minus) use { tokenToOp[this.type]!! }
    val arithmetic: Parser<CyanExpression> by leftAssociative(mulDivModOrTerm, -optional(ws) * plusMinusOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r) }

    val comparisonOp by deq or neq or lt or leq or gt or geq
    val comparisonOrMath: Parser<CyanExpression> by (arithmetic * optional(comparisonOp * arithmetic))
            .map { (left, tail) -> tail?.let { (op, r) -> CyanBinaryExpression(left, tokenToOp[op.type]!!, r) } ?: left }

    val andChain by leftAssociative(comparisonOrMath, -optional(ws) * and * -optional(ws)) { l, _, r -> CyanBinaryExpression(l, CyanBinaryAndOperator, r) }
    val orChain  by leftAssociative(andChain, -optional(ws) * or * -optional(ws)) { l, _, r -> CyanBinaryExpression(l, CyanBinaryOrOperator, r) }

    val expr by orChain

    // Scope

    val block by (-lcur * -znws * parser(this::rootParser) * -znws * -rcur)

    // Functions

    val functionArgument  by (referenceParser * -znws * typeSignature) use { CyanFunctionArgument(t1.value, t2) }
    val functionSignature by (optional(extern) * -znws * -function * -znws * referenceParser * -znws * -leap * separatedTerms(functionArgument, commaParser, true) * -reap * -znws * optional(typeSignature))
            .use { t4?.let { CyanFunctionSignature(t2, t3, it, isExtern = t1 != null) } ?: CyanFunctionSignature(t2, t3, isExtern = t1 != null) }

    val functionDeclaration: Parser<CyanFunctionDeclaration> by (functionSignature * -znws * optional(block))
            .use { CyanFunctionDeclaration(t1, t2) }

    // Statements

    val variableSignature      by ((let or vark) * -znws * referenceParser * -znws * optional(typeSignature))
    val variableInitialization by (-znws * -assign * -znws * expr)
    val variableDeclaration    by (variableSignature and variableInitialization)
        .use { CyanVariableDeclaration(t1.t2, t1.t1.type == vark, t1.t3, t2) }

    val functionCall           by (referenceParser * -leap * -znws * separatedTerms(expr, commaParser, true) * -znws * -reap) use { CyanFunctionCall(t1, t2.toTypedArray()) }

    val ifStatementSignature                    by (-ifToken * -znws * -leap * expr * -reap)
    val ifStatement: Parser<CyanIfStatement>    by (ifStatementSignature * -znws * block).use { CyanIfStatement(t1, t2) }
    val elseStatement: Parser<CyanSource>       by (-elseToken * -znws * block)
    val ifStatementChain: Parser<CyanStatement> by (separatedTerms(ifStatement, -optional(ws) * elseToken * -znws) * optional(-znws * elseStatement))
            .use { CyanIfChain(t1.toTypedArray(), elseBlock = t2) }

    val assignStatement: Parser<CyanStatement>  by (referenceParser * -znws * -assign * -znws * expr) use { CyanAssignment(t1, t2) }

    val statement by -optional(ws) * (variableDeclaration or functionDeclaration or typeDeclaration or functionCall or ifStatementChain or assignStatement) * -optional(ws)

    // Root parser

    val sourceParser = separatedTerms(statement, znws) use { CyanSource(this) }

    override val rootParser = sourceParser

}
