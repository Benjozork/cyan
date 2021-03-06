package cyan.compiler.parser

import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.types.CyanTypeAnnotation
import cyan.compiler.parser.ast.types.CyanStructDeclaration
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.*

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import cyan.compiler.codegen.wasm.dsl.block

import cyan.compiler.parser.ast.function.*
import cyan.compiler.parser.ast.types.CyanTraitDeclaration
import cyan.compiler.parser.grammars.NumericLiteralParser
import cyan.compiler.parser.util.DotChainAssociator
import cyan.compiler.parser.util.span

@Suppress("MemberVisibilityCanBePrivate")
class CyanModuleParser : Grammar<CyanModule>() {

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
    val forToken        by regexToken("for\\b")
    val ofToken         by regexToken("of\\b")
    val deriveToken     by regexToken("derive\\b")

    val trueToken       by regexToken("true\\b")
    val falseToken      by regexToken("false\\b")

    // Complex types

    val type            by regexToken("type\\b")
    val struct          by regexToken("struct\\b")
    val trait           by regexToken("trait")

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

    // Comparison

    val deq             by literalToken("==")
    val neq             by literalToken("!=")
    val leq             by literalToken("<=")
    val lt              by literalToken("<")
    val geq             by literalToken(">=")
    val gt              by literalToken(">")

    // Misc. syntax

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

    val nws                by oneOrMore(newLine or ws)
    val znws               by zeroOrMore(newLine or ws) // Zero or more Newlines or WhiteSpaces
    val commaParser        by comma and znws

    // Type base parsers

    val primTypeName  by (anyPrim or voidPrim or int8Prim or int32Prim or int64Prim or float32Prim or float64Prim or boolPrim or strPrim or charPrim)
    val litType       by (primTypeName * optional(arraySuffix)) use { CyanTypeAnnotation.Literal(Type.Primitive(tokenToType[t1.type]!!, t2 != null), t2?.let { span(t1, t2!!) } ?: span(t1)) }
    val refType       by (parser(this::referenceParser))        use { CyanTypeAnnotation.Reference(this, this.span) }
    val typeSignature by (-colon * -znws * (litType or refType))

    // Complex type parsers

    val typePrefix by (-type * -znws * parser(this::referenceParser) * -znws * -assign)

    // structs

    val structProperty    by (parser(this::referenceParser) * typeSignature)              use { CyanStructDeclaration.Property(t1, t2, span(t1, t2)) }
    val structProperties  by separatedTerms(structProperty, commaParser)                  use { toTypedArray() }
    val structDerives     by separatedTerms(parser(this::derive), nws, acceptZero = true) use { toTypedArray() }
    val structDeclaration by (typePrefix * -znws * -struct * -znws * -lcur * -znws * structProperties * -nws * structDerives * -znws * -rcur)
        .use { CyanStructDeclaration(t1, t2, t3, span(t1, t3.lastOrNull() ?: t2.last())) }

    // traits

    val traitFunction    by parser(this::functionSignature) use { CyanTraitDeclaration.Element.Function(CyanFunctionDeclaration(this, null), span) }
    val traitProperty    by structProperty                  use { CyanTraitDeclaration.Element.Property(ident, type, span) }
    val traitElement     by (traitFunction or traitProperty)
    val traitDeclaration by (typePrefix * -znws * -trait * -znws * -lcur * -znws * (separatedTerms(traitElement, nws) use { toTypedArray() }) * -znws * -rcur)
        .use { CyanTraitDeclaration(t1, t2, span(t1, t2.last())) }

    val complexType by (structDeclaration or traitDeclaration)

    // Derives

    val deriveFunctionImpl: Parser<CyanDerive.Item.Function> by (parser(this::referenceParser) * -znws * parser(this::functionArguments) * typeSignature * -znws * parser(this::block))
        .use { CyanDerive.Item.Function(t1, t2.t1.toTypedArray(), t3, t4, span(t1, t4)) }

    val deriveImpl: Parser<CyanDerive.Item> by (deriveFunctionImpl)
    val deriveBody                          by (-lcur * -znws * (separatedTerms(deriveImpl, nws) use { toTypedArray() }) * -znws * -rcur)
    val derive                              by (deriveToken * -znws * refType * -znws * deriveBody) use { CyanDerive(t2, t3, span(t1, t3.last())) }

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

    val and by literalToken("&&")
    val or  by literalToken("||")

    // Values

    val ident by regexToken("[a-zA-Z_]([a-zA-Z_0-9]+)?")

    val numericLiteralParser = NumericLiteralParser(minus)
    val numericalValueParser by numericLiteralParser

    val stringLiteral   by regexToken("\".*?\"")

    // Value parsers

    val referenceParser      by ident                     use { CyanIdentifierExpression(text, span(this)) }
    val stringLiteralParser  by stringLiteral             use { CyanStringLiteralExpression(text.removeSurrounding("\""), span(this)) }
    val booleanLiteralParser by (trueToken or falseToken) use { CyanBooleanLiteralExpression(type == trueToken, span(this)) }

    // Expressions

    val literalExpressionParser by (numericalValueParser or stringLiteralParser or booleanLiteralParser)

    val emptyArray            by (arraySuffix)                                                                       use { CyanArrayExpression(emptyArray(), span(this)) }
    val nonEmptyArray         by (lsq * -znws * separatedTerms(parser(this::expr), commaParser, true) * -znws * rsq) use { CyanArrayExpression(t2.toTypedArray(), span(t1, t3)) }
    val arrayExpressionParser by (emptyArray or nonEmptyArray)

    val parenTerm = (-leap * parser(this::expr) * -reap)

    val unambiguousTermForFunctionCall: Parser<CyanExpression>
            by (parenTerm or referenceParser)

    val term: Parser<CyanExpression>
            by (parenTerm or arrayExpressionParser or literalExpressionParser or referenceParser)

    val indexedTerm: Parser<CyanExpression>
            by (term * -znws * lsq * parser(this::expr) * -znws * rsq) use { CyanArrayIndexExpression(t1, t3, span(t1, t4)) }

    val callArgument        by (optional(referenceParser * -colon) * -znws * parser(this::expr))
    val calledTermArguments by separatedTerms(callArgument, commaParser, acceptZero = true) map { l -> l.map { CyanFunctionCall.Argument(it.t1, it.t2) } }
    val calledTerm: Parser<CyanExpression>
            by (term * -znws * -leap * calledTermArguments * reap) use { CyanFunctionCall(t1, t2.toTypedArray(), span(t1, t3)) }

    val atom by (indexedTerm or calledTerm or term)

    val termDotChain: Parser<CyanExpression> by separatedTerms(atom, (-znws * dot * -znws), acceptZero = false) use { DotChainAssociator.associate(this) }

    val mulDivModOp by (times or div or mod) use { tokenToOp[this.type]!! }
    val mulDivModOrTerm: Parser<CyanExpression>
            by leftAssociative(termDotChain, -optional(ws) * mulDivModOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r, span(l, r)) }

    val plusMinusOp by (plus or minus) use { tokenToOp[this.type]!! }
    val arithmetic: Parser<CyanExpression>
            by leftAssociative(mulDivModOrTerm, -optional(ws) * plusMinusOp * -optional(ws)) { l, o, r -> CyanBinaryExpression(l, o, r, span(l, r)) }

    val comparisonOp by deq or neq or lt or leq or gt or geq
    val comparisonOrMath: Parser<CyanExpression>
            by (arithmetic * optional(-znws * comparisonOp * -znws * arithmetic))
            .map { (left, tail) -> tail?.let { (op, r) -> CyanBinaryExpression(left, tokenToOp[op.type]!!, r, span(left, r)) } ?: left }

    val andChain by leftAssociative(comparisonOrMath, -optional(ws) * and * -optional(ws)) { l, _, r -> CyanBinaryExpression(l, CyanBinaryAndOperator, r, span(l, r)) }
    val orChain  by leftAssociative(andChain, -optional(ws) * or * -optional(ws))          { l, _, r -> CyanBinaryExpression(l, CyanBinaryOrOperator, r, span(l, r)) }

    val expr by orChain

    // Scope

    val block by (lcur * -znws * parser(this::sourceParser) * -znws * rcur) use { CyanSource(t2.statements, span(t1, t3)) }

    // Function attributes

    val valueAttribute   by (referenceParser * -znws * -assign * -znws * expr) use { CyanFunctionAttribute.Value(t1, t2, span(t1, t2)) }
    val keywordAttribute by (referenceParser)                                  use { CyanFunctionAttribute.Keyword(this, span(this)) }

    // Functions

    val functionAttribute  by (valueAttribute or keywordAttribute)
    val functionAttributes by (lsq * separatedTerms(functionAttribute, commaParser, false) * rsq) use { t2 }
    val functionReceiver   by (leap * (litType or refType) * -reap * dot)                         use { CyanFunctionReceiver(t2, span(t1, t3)) }
    val functionArgument   by (referenceParser * -znws * typeSignature)                           use { CyanFunctionArgument(t1.value, t2, span(t1, t2)) }
    val functionArguments  by (-leap * separatedTerms(functionArgument, commaParser, true) * reap)
    val functionSignature  by (optional(functionAttributes * -znws) * optional(extern) * -znws * function * -znws * optional(functionReceiver) * referenceParser * -znws * functionArguments * -znws * optional(typeSignature))
            .use {
                val spanStart = t1?.first()?.span?.fromTokenMatches?.first() ?: t2 ?: t3
                val spanEnd = t7?.span?.fromTokenMatches?.first() ?: t6.t2

                if (t7 != null)
                    CyanFunctionSignature(t1 ?: emptyList(), t4, t5, t6.t1, t7!!, isExtern = t2 != null, span = span(spanStart, spanEnd))
                else
                    CyanFunctionSignature(t1 ?: emptyList(), t4, t5, t6.t1, isExtern = t2 != null, span = span(spanStart, spanEnd))
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

    // For statements

    val forStatementIterator by (referenceParser * -znws * -ofToken * -znws * expr)
    val forStatement: Parser<CyanStatement> by (forToken * -znws * forStatementIterator * -znws * block) use { CyanForStatement(t2.t1, t2.t2, t3, span(t1, t3)) }

    // Assignment

    val assignmentBase by (indexedTerm or referenceParser)
    val assignStatement: Parser<CyanStatement> by (assignmentBase * -znws * -assign * -znws * expr) use { CyanAssignment(t1, t2, span(t1, t2)) }

    // Return

    val returnStatement: Parser<CyanReturn> by (returnToken * -znws * expr) use { CyanReturn(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // Import

    val importStatement by (import * -znws * referenceParser) use { CyanImportStatement(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // Module declaration

    val moduleDeclaration by (module * -znws * referenceParser) use { CyanModuleDeclaration(t2, span(t1, t2.span!!.fromTokenMatches.last())) }

    // StatementscomplexType

    val anyStatement
            by -znws * (importStatement or variableDeclaration or functionDeclaration or complexType or ifStatementChain or whileStatement or forStatement or functionCall or assignStatement or returnStatement) * -znws

    // Source parser

    val sourceParser by separatedTerms(anyStatement, znws) use { CyanSource(this, span(this.first().span!!, this.last().span!!)) }

    // Module parser

    val moduleParser by (moduleDeclaration * -znws * sourceParser) use { CyanModule(t1, t2, span(t1, t2)) }

    override val rootParser = moduleParser

    override val tokens: List<Token>
        get() = super.tokens + numericLiteralParser.tokens

}
