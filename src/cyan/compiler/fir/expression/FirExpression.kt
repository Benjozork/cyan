package cyan.compiler.fir.expression

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionReceiver
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.operator.CyanBinaryOperator
import cyan.compiler.parser.ast.operator.CyanBinaryComparisonOperator

import com.andreapivetta.kolor.lightGray

open class FirExpression(override var parent: FirNode, val fromAstNode: CyanExpression) : FirNode {

    abstract class Literal(override var parent: FirNode, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode) {

        interface Scalar<TValue : Any> {
            val value: TValue
        }

        class Number(override val value: Int, parent: FirNode, fromAstNode: CyanExpression) : Literal(parent, fromAstNode), Scalar<kotlin.Number>

        class String(override val value: kotlin.String, parent: FirNode, fromAstNode: CyanExpression) : Literal(parent, fromAstNode), Scalar<kotlin.String>

        class Boolean(override val value: kotlin.Boolean, parent: FirNode, fromAstNode: CyanExpression) : Literal(parent, fromAstNode), Scalar<kotlin.Boolean>

        class Array(val elements: List<FirExpression>, parent: FirNode, fromAstNode: CyanExpression) : Literal(parent, fromAstNode)

        class Struct(val elements: Map<Type.Struct.Property, FirExpression>, val type: Type.Struct, parent: FirNode, fromAstNode: CyanExpression) : Literal(parent, fromAstNode)

    }

    class Binary(var lhs: FirExpression, var operator: CyanBinaryOperator, var rhs: FirExpression, parent: FirNode, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode) {
        val commonType get() = if (lhs.realExpr.type() == rhs.realExpr.type()) lhs.realExpr.type() else null
    }

    class MemberAccess(val base: FirExpression, val member: String, parent: FirNode, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode)

    class ArrayIndex(val base: FirExpression, val index: FirExpression, parent: FirNode, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode)

    class FunctionCall(parent: FirNode, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode), FirStatement {

        lateinit var callee: FirResolvedReference

        var receiver: FirExpression? = null

        val args = mutableListOf<FirExpression>()

    }

    /**
     * Contains the inline expression if an expression was inlined to replace this expression.
     */
    var inlinedExpr: FirExpression? = null

    /**
     * Infers the type of this FirExpression node.
     */
    fun type(): Type {
         return when (this) {
             is Literal.Number  -> Type.Primitive(CyanType.I32)
             is Literal.String  -> Type.Primitive(CyanType.Str)
             is Literal.Boolean -> Type.Primitive(CyanType.Bool)
             is FunctionCall -> {
                 val firFunctionDeclaration = callee.resolvedSymbol as FirFunctionDeclaration

                 val functionDeclarationArgsToPassedArgs = (firFunctionDeclaration.args zip args).toMap()

                 functionDeclarationArgsToPassedArgs.entries.forEachIndexed { i, (firArg, astArg) -> // Type check args
                     val astArgType = astArg.type()

                     if (!(firArg.typeAnnotation accepts astArgType)) {
                         DiagnosticPipe.report (
                             CompilerDiagnostic (
                                 level = CompilerDiagnostic.Level.Error,
                                 message = "Type mismatch for argument $i: expected '${firArg.typeAnnotation}', found '${astArgType}'",
                                 astNode = fromAstNode
                             )
                         )
                     }
                 }

                 firFunctionDeclaration.returnType
             }
             is Literal.Struct -> type
             is Literal.Array -> elements.first().type().asArrayType()
             is Binary -> {
                 val (lhsType, rhsType) = lhs.type() to rhs.type()

                 if (lhsType != rhsType) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "binary expressions with different operand types are not yet supported",
                             astNode = fromAstNode
                         )
                     )
                 }

                 if (operator is CyanBinaryComparisonOperator) {
                     Type.Primitive(CyanType.Bool, false)
                 } else lhsType
             }
             is FirResolvedReference -> {
                 val containingScope = this.containingScope()

                 when (val referee = containingScope?.findSymbol(this.reference())?.resolvedSymbol) {
                     is FirVariableDeclaration -> referee.initializationExpr.type()
                     is FirFunctionDeclaration -> Type.Primitive(CyanType.Any, false)
                     is FirFunctionArgument -> referee.typeAnnotation
                     is FirFunctionReceiver -> referee.type
                     null -> error("cannot find symbol '${text}'")
                     else -> error("can't infer type of ${referee::class.simpleName}")
                 }
             }
             is MemberAccess -> {
                 val baseType = base.type()
                 val memberName = member

                 when (baseType) {
                     is Type.Primitive -> DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "Primitives do not have members",
                             astNode = fromAstNode
                         )
                     )
                     is Type.Struct -> {
                         val matchingStructProperty = baseType.properties.firstOrNull { it.name == memberName }
                             ?: DiagnosticPipe.report (
                                 CompilerDiagnostic (
                                     level = CompilerDiagnostic.Level.Error,
                                     message = "Type '${baseType.name}' does not have a member called '$memberName'",
                                     astNode = fromAstNode, span = (fromAstNode as CyanMemberAccessExpression).member.span,
                                     note = CompilerDiagnostic.Note("type '${baseType.name}' is defined as follows\n" + "    | ".lightGray() + "$baseType")
                                 )
                             )

                         matchingStructProperty.type
                     }
                 }
             }
             is ArrayIndex -> {
                 val baseExprType = base.type()

                 if (!baseExprType.array) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "left-hand side of array index expression must refer to an array",
                             astNode = fromAstNode
                         )
                     )
                 }

                 when (baseExprType) {
                     is Type.Primitive -> Type.Primitive(baseExprType.base, false)
                     is Type.Struct    -> Type.Struct(baseExprType.name, baseExprType.properties, false)
                 }
             }
             else -> error("can't infer type of ${this::class.simpleName}")
        }
    }

    override fun allReferredSymbols(): Set<FirResolvedReference> = when (this) {
        is Literal.Number,
        is Literal.String,
        is Literal.Boolean -> emptySet()
        is FunctionCall -> setOf(callee) + args.flatMap { it.allReferredSymbols() }
        is Literal.Struct -> elements.flatMap { it.value.allReferredSymbols() }.toSet()
        is Literal.Array -> elements.flatMap { it.allReferredSymbols() }.toSet()
        is Binary -> lhs.allReferredSymbols() + rhs.allReferredSymbols()
        is FirResolvedReference -> setOf(this)
        is MemberAccess -> base.allReferredSymbols()
        is ArrayIndex -> base.allReferredSymbols()
        else -> error("cannot find resolved symbols for expression of type '${this::class.simpleName}'")
    }

    /**
     * Whether or not this expression is pure (doesn't have any side effects on evaluation). For example, an addition of two constants is
     * reducible, as well as a simple reference to another variable. A regular function call, however, is not.
     */
    val isPure: Boolean by lazy { this.isConstant || when (this) { // Note: this is processed based on current language capabilities
        is Binary -> lhs.isPure && rhs.isPure
        is FirResolvedReference -> true
        is Literal.Array -> elements.all { it.isPure }
        is MemberAccess -> {
            if (base !is FirResolvedReference) false else when (val symbol = base.resolvedSymbol) {
                is FirVariableDeclaration -> symbol.initializationExpr.type() is Type.Struct
                is FirFunctionDeclaration -> symbol.returnType is Type.Struct
                is FirFunctionArgument    -> symbol.typeAnnotation is Type.Struct
                else -> error("cannot check for return type of '${symbol::class.simpleName}'")
            }
        }
        is ArrayIndex -> base.isPure
        else -> false
    }}

    /**
     * Whether or not this expression is constant.
     */
    val isConstant: Boolean get() = when (val expr = this.realExpr) {
        is Literal.Array -> expr.elements.all { it.isConstant }
        is Literal.Struct -> expr.elements.all { it.value.isConstant }
        is Literal -> true
        is Binary -> expr.lhs.isConstant && expr.rhs.isConstant
        is ArrayIndex -> expr.base.isConstant && expr.index.isConstant
        else -> false
    }

    val realExpr get() = inlinedExpr ?: this

}
