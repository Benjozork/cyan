package cyan.compiler.parser

import cyan.compiler.common.Span
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.types.CyanTypeAnnotation
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
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

@Suppress("MemberVisibilityCanBePrivate")
class CyanModuleParser : Grammar<CyanModule>() {

    fun span(begin: TokenMatch, end: TokenMatch) = Span(begin.row, begin.column..(end.column + end.length), arrayOf(begin, end))

    fun span(whole: TokenMatch) = Span(whole.row, whole.column..(whole.column + whole.length), arrayOf(whole))

    fun span(expression: CyanExpression) = span(expression.span!!.fromTokenMatches.first(), expression.span!!.fromTokenMatches.last())

    fun span(firstExpression: CyanItem, secondExpression: CyanItem) =
        span(firstExpression.span!!.fromTokenMatches.first(), secondExpression.span!!.fromTokenMatches.last())

    fun span(firstExpression: CyanItem, secondTokenMatch: TokenMatch) =
        span(firstExpression.span!!.fromTokenMatches.first(), secondTokenMatch)

    fun span(firstExpression: TokenMatch, secondTokenMatch: CyanItem) =
            span(firstExpression, secondTokenMatch.span!!.fromTokenMatches.first())

    fun span(firstSpan: Span, secondSpan: Span) = span(firstSpan.fromTokenMatches.first(), secondSpan.fromTokenMatches.last())

    // Tokens

    val newLine         by regexToken("\n|\r\n")
    val ws              by regexToken("\\s+")

    val module          by regexToken("module\\b")
    val import          by regexToken("import\\b")
    val let             by regexToken("let\\b")
    val vark            by regexToken("var\\b")
    val extern          by regexToken("extern\\b")
    val function        by regexToken("function\\b")
    val returnToken     by regexToken("return\\b")
    val ifToken         by regexToken("if\\b")
    val elseToken       by regexToken("else\\b")
    val whileToken      by regexToken("while\\b")

    val trueToken       by regexToken("true\\b")
    val falseToken      by regexToken("false\\b")

    // Complex types

    val type            by regexToken("type\\b")
    val struct          by regexToken("struct\\b")

    // Primitive types

    val anyPrim         by regexToken("any\\b")
    val voidPrim        by regexToken("void\\b")
    val int8Prim        by regexToken("i8\\b")
    val int32Prim       by regexToken("i32\\b")
    val int64Prim       by regexToken("i64\\b")
    val float32Prim     by regexToken("f32\\b")
    val float64Prim     by regexToken("f64\\b")
    val boolPrim        by regexToken("bool\\b")
    val strPrim         by regexToken("str\\b")
    val charPrim        by regexToken("char\\b")

    val tokenToType = mapOf (
        anyPrim     to CyanType.Any,
        voidPrim    to CyanType.Void,
        int8Prim    to CyanType.I8,
        int32Prim   to CyanType.I32,
        int64Prim   to CyanType.I64,
        float32Prim to CyanType.F32,
        float64Prim to CyanType.F64,
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
    val litType       by (primTypeName * optional(arraySuffix)) use { CyanTypeAnnotation.Literal(Type.Primitive(tokenToType[t1.type]!!, t2 != null), t2?.let { span(t1, t2!!) } ?: span(t1)) }
    val refType       by (parser(this::referenceParser))        use { CyanTypeAnnotation.Reference(this, this.span) }
    val typeSignature by (-optional(colon) * -znws * (litType or refType))

    // Complex type parsers

    val structProperty    by (parser(this::referenceParser) * typeSignature) use { CyanStructDeclaration.Property(t1, t2, span(t1, t2)) }
    val structProperties  by separatedTerms(structProperty, commaParser)     use { this.toTypedArray() }
    val structBody        by (-lcur * -znws * structProperties * -znws * -rcur)
    val structDeclaration by (-struct * -znws * structBody)

    val complexType by (structDeclaration)

    val typeDeclaration by (type * -znws * parser(this::referenceParser) * -znws * -assign * -znws * complexType) use { CyanStructDeclaration(t2, t3, span(t1, t3.last().span!!.fromTokenMatches.last())) }

    // Arithmetic

    val plus            by literalToken("+")
    val minus           by literalToken("-")
    val times           by literalToken("*")
    val div             by literalToken("/")
    val mod             by literalToken("%")
    val exp             by literalToken("^")

    // Comparison

    val deq             by literalToken("==")
    val neq             by literalToken("!=")
    val leq             by literalToken("<=")
    val lt              by literalToken("<")
    val geq             by literalToken(">=")
    val gt              by literalToken(">")

    val tokenToOp = mapOf (
        plus  to CyanBinaryPlusOperator,
        minus to CyanBinaryMinusOperator,
        times to CyanBinaryTimesOperator,
        div   to CyanBinaryDivOperator,
        mod   to CyanBinaryModOperator,
        exp   to CyanBinaryExpOperator,
        deq   to CyanBinaryEqualsOperator,
        neq   to CyanBinaryNotEqualsOperator,
        leq   to CyanBinaryLesserEqualsOperator,
        lt    to CyanBinaryLesserOperator,
        geq   to CyanBinaryGreaterEqualsOperator,
        gt    to CyanBinaryGreaterOperator
    )

    // Boolean

    val and             by literalToken("&&")
    val or              by literalToken("||")

    // Values

    val ident           by regexToken("[a-zA-Z_]+")
    val numericalValue  by regexToken("\\d+")

    val stringLiteral   by regexToken("\".*?\"")

    // Value parsers

    val referenceParser      by ident                     use { CyanIdentifierExpression(text, span(this)) }
    val stringLiteralParser  by stringLiteral             use { CyanStringLiteralExpression(text.removeSurrounding("\""), span(this)) }
    val numericalValueParser by numericalValue            use { CyanNumericLiteralExpression(text.toInt(), span(this)) }
    val booleanLiteralParser by (trueToken or falseToken) use { CyanBooleanLiteralExpression(type == trueToken, span(this)) }

    // Struct literals

    val structLiteralParser by (optional(refType) * -znws * lcur * -znws * separatedTerms(parser(this::expr), commaParser) * -znws * rcur)
        .use { CyanStructLiteralExpression(t3.toTypedArray(), t1, t1?.let { span(t1!!, t4) } ?: span(t2, t4)) }

    // Members

    val memberAccessParser by (referenceParser * -dot * referenceParser)                                   use { CyanMemberAccessExpression(t1, t2, span(t1, t2)) }
    val arrayIndexParser   by (parser(this::unambiguousTermForArrayIndex) * -znws * -lsq * parser(this::expr) * rsq) use { CyanArrayIndexExpression(t1, t2, span(t1, t3)) }

    // Expressions

    val literalExpressionParser by (numericalValueParser or stringLiteralParser or booleanLiteralParser or structLiteralParser)

    val arrayExpressionParser by (lsq * separatedTerms(parser(this::expr), commaParser, true) * rsq) use { CyanArrayExpression(t2.toTypedArray(), span(t1, t3)) }

    val parenTerm = (-leap * parser(this::expr) * -reap)

    val unambiguousTermForFunctionCall: Parser<CyanExpression>
            by (parenTerm or memberAccessParser or referenceParser)

    val unambiguousTermForArrayIndex: Parser<CyanExpression>
            by (parenTerm or memberAccessParser or parser(this::functionCall) or referenceParser)

    val term: Parser<CyanExpression>
            by (parenTerm or arrayExpressionParser or literalExpressionParser or parser(this::functionCall) or memberAccessParser or arrayIndexParser or referenceParser)

    val mulDivModOp by (times or div or mod) use { tokenToOp[this.type]!! }
    val mulDivModOrTerm: Parser<CyanExpression> by leftAssociative(term, -optional(ws) * mulDivModOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r, span(l, r)) }

    val plusMinusOp by (plus or minus) use { tokenToOp[this.type]!! }
    val arithmetic: Parser<CyanExpression> by leftAssociative(mulDivModOrTerm, -optional(ws) * plusMinusOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r, span(l, r)) }

    val comparisonOp by deq or neq or lt or leq or gt or geq
    val comparisonOrMath: Parser<CyanExpression> by (arithmetic * optional(-znws * comparisonOp * -znws * arithmetic))
            .map { (left, tail) -> tail?.let { (op, r) -> CyanBinaryExpression(left, tokenToOp[op.type]!!, r, span(left, r)) } ?: left }

    val andChain by leftAssociative(comparisonOrMath, -optional(ws) * and * -optional(ws)) { l, _, r -> CyanBinaryExpression(l, CyanBinaryAndOperator, r, span(l, r)) }
    val orChain  by leftAssociative(andChain, -optional(ws) * or * -optional(ws))          { l, _, r -> CyanBinaryExpression(l, CyanBinaryOrOperator, r, span(l, r)) }

    val expr by orChain

    // Scope

    val block by (lcur * -znws * parser(this::sourceParser) * -znws * rcur) use { CyanSource(t2.statements, span(t1, t3)) }

    // Functions

    val functionArgument  by (referenceParser * -znws * typeSignature) use { CyanFunctionArgument(t1.value, t2, span(t1, t2)) }
    val functionSignature by (optional(extern) * -znws * function * -znws * referenceParser * -znws * -leap * separatedTerms(functionArgument, commaParser, true) * reap * -znws * optional(typeSignature))
            .use {
                val spanStart = t1 ?: t2
                val spanEnd = t6?.span?.fromTokenMatches?.first() ?: t5

                if (t6 != null)
                    CyanFunctionSignature(t3, t4, t6!!, isExtern = t1 != null, span = span(spanStart, spanEnd))
                else
                    CyanFunctionSignature(t3, t4, isExtern = t1 != null, span = span(spanStart, spanEnd))
            }

    val functionDeclaration: Parser<CyanFunctionDeclaration> by (functionSignature * -znws * optional(block))
            .use { CyanFunctionDeclaration(t1, t2, t2?.let { span(t1.span!!, t2!!.span!!) } ?: t1.span) }

    // Variables

    val variableSignature      by ((let or vark) * -znws * referenceParser * -znws * optional(typeSignature))
    val variableInitialization by (-znws * -assign * -znws * expr)
    val variableDeclaration    by (variableSignature and variableInitialization)
        .use { CyanVariableDeclaration(t1.t2, t1.t1.type == vark, t1.t3, t2, span(t1.t1, t2.span!!.fromTokenMatches.last())) }

    // Function calls

    val functionCallArgument  by (optional(referenceParser * -colon) * -znws * expr) use { CyanFunctionCall.Argument(t1, t2, t1?.let { span(t1!!, t2) } ?: t2.span ) }
    val functionCallArguments by separatedTerms(functionCallArgument, commaParser, true)
    val functionCall          by (unambiguousTermForFunctionCall * -znws * -leap * -znws * functionCallArguments * -znws * reap)
            .use { CyanFunctionCall(t1, t2.toTypedArray(), span(t1, t3)) }

    // If statements

    val ifStatementSignature                    by (-ifToken * -znws * -leap * expr * -reap)
    val ifStatement: Parser<CyanIfStatement>    by (ifStatementSignature * -znws * block).use { CyanIfStatement(t1, t2, span(t1, t2)) }
    val elseStatement: Parser<CyanSource>       by (-elseToken * -znws * block)
    val ifStatementChain: Parser<CyanStatement> by (separatedTerms(ifStatement, -optional(ws) * elseToken * -znws) * optional(-znws * elseStatement))
            .use { CyanIfChain(t1.toTypedArray(), elseBlock = t2, span = span(t1.first().span!!, t2?.span ?: t1.last().span!!)) }

    // While statements

    val whileStatement: Parser<CyanStatement> by (whileToken * -znws * expr * -znws * block) use { CyanWhileStatement(t2, t3, span(t1, t3)) }

    // Assignment

    val assignStatement: Parser<CyanStatement>  by (referenceParser * -znws * -assign * -znws * expr) use { CyanAssignment(t1, t2, span(t1, t2)) }

    // Return

    val returnStatement: Parser<CyanReturn> by (returnToken * -znws * expr) use { CyanReturn(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // Import

    val importStatement by (import * -znws * referenceParser) use { CyanImportStatement(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // Module declaration

    val moduleDeclaration by (module * -znws * referenceParser) use { CyanModuleDeclaration(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // Statements

    val anyStatement
            by -znws * (importStatement or variableDeclaration or functionDeclaration or typeDeclaration or ifStatementChain or whileStatement or functionCall or assignStatement or returnStatement) * -znws

    // Source parser

    val sourceParser by separatedTerms(anyStatement, znws) use { CyanSource(this, span(this.first().span!!, this.last().span!!)) }

    // Module parser

    val moduleParser by (moduleDeclaration * -znws * sourceParser) use { CyanModule(t1, t2, span(t1, t2)) }

    override val rootParser = moduleParser

}
