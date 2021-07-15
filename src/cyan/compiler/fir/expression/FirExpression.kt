package cyan.compiler.fir.expression

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.operator.CyanBinaryOperator
import cyan.compiler.parser.ast.operator.CyanBinaryComparisonOperator

import com.andreapivetta.kolor.lightGray
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

abstract class FirExpression(override var parent: FirNode, val fromAstNode: CyanExpression) : FirNode, FirTyped {

    abstract class Literal (
        override var parent: FirNode,
        private val forType: Type,
        fromAstNode: CyanExpression
    ) : FirExpression(parent, fromAstNode) {

        override fun type() = forType

        abstract class Scalar<TValue : Any> (
            override var parent: FirNode,
            forType: Type,
            fromAstNode: CyanExpression
        ): Literal(parent, forType, fromAstNode) {

            abstract val value: TValue

            override fun allReferredSymbols(): Set<FirResolvedReference> = emptySet()

        }

        class Number (
            override val value: Int,
            parent: FirNode,
            fromAstNode: CyanExpression
        ) : Scalar<kotlin.Number>(parent, Type.Primitive(CyanType.I32), fromAstNode)

        class String (
            override val value: kotlin.String,
            parent: FirNode,
            fromAstNode: CyanExpression
        ) : Scalar<kotlin.String>(parent, Type.Primitive(CyanType.Str), fromAstNode)

        class Boolean (
            override val value: kotlin.Boolean,
            parent: FirNode,
            fromAstNode: CyanExpression
        ) : Scalar<kotlin.Boolean>(parent, Type.Primitive(CyanType.Bool), fromAstNode)

        class Array (
            val elements: List<FirExpression>,
            parent: FirNode,
            fromAstNode: CyanExpression
        ) : Literal(parent, elements.firstOrNull()?.type()?.asArrayType() ?: Type.Primitive(CyanType.Any, true), fromAstNode) {

            override fun allReferredSymbols() = elements.flatMap { it.allReferredSymbols() }.toSet()

        }

        class Struct (
            val elements: Map<Type.Struct.Property, FirExpression>,
            val type: Type.Struct,
            parent: FirNode,
            fromAstNode: CyanExpression
        ) : Literal(parent, type, fromAstNode) {

            override fun allReferredSymbols() = elements.flatMap { it.value.allReferredSymbols() }.toSet()

        }

    }

    class Binary (
        var lhs: FirExpression,
        var operator: CyanBinaryOperator,
        var rhs: FirExpression,
        parent: FirNode,
        fromAstNode: CyanExpression
    ) : FirExpression(parent, fromAstNode) {

        val commonType get() = if (lhs.realExpr.type() == rhs.realExpr.type()) lhs.realExpr.type() else null

        override fun type(): Type {
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

            return if (operator is CyanBinaryComparisonOperator) {
                Type.Primitive(CyanType.Bool, false)
            } else lhsType
        }

        override fun allReferredSymbols() = lhs.allReferredSymbols() + rhs.allReferredSymbols()

    }

    class MemberAccess (
        val base: FirExpression,
        val member: String,
        parent: FirNode,
        fromAstNode: CyanExpression
    ) : FirExpression(parent, fromAstNode) {

        override fun type(): Type {
            val baseType = base.type()
            val memberName = member

            return when (baseType) {
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
                                astNode = fromAstNode,
                                span = (fromAstNode as CyanMemberAccessExpression).member.span,
                                note = CompilerDiagnostic.Note("type '${baseType.name}' is defined as follows\n" + "    | ".lightGray() + "$baseType")
                            )
                        )

                    matchingStructProperty.type
                }
                is Type.Trait -> {
                    val matchingTraitElement = baseType.elements.firstOrNull { it.name == memberName }
                        ?: DiagnosticPipe.report (
                            CompilerDiagnostic (
                                level = CompilerDiagnostic.Level.Error,
                                message = "Type '${baseType.name}' does not have a member called '$memberName'",
                                astNode = fromAstNode,
                                span = (fromAstNode as CyanMemberAccessExpression).member.span,
                                note = CompilerDiagnostic.Note("type '${baseType.name}' is defined as follows\n" + "    | ".lightGray() + "$baseType")
                            )
                        )

                    matchingTraitElement.returnType
                }
                is Type.Self -> {
                    val resolvedStructType = baseType.resolveIn(this)

                    val matchingStructMember = resolvedStructType.properties.firstOrNull { it.name == memberName }
                        ?: DiagnosticPipe.report (
                            CompilerDiagnostic (
                                level = CompilerDiagnostic.Level.Error,
                                message = "Type '${resolvedStructType.name}' does not have a member called '$memberName'",
                                astNode = fromAstNode,
                                span = (fromAstNode as CyanMemberAccessExpression).member.span,
                                note = CompilerDiagnostic.Note("type '${resolvedStructType.name}' is defined as follows\n" + "    | ".lightGray() + "$resolvedStructType")
                            )
                        )

                    matchingStructMember.type
                }
            }
        }

        override fun allReferredSymbols() = base.allReferredSymbols()

    }

    class ArrayIndex (
        val base: FirExpression,
        val index: FirExpression,
        parent: FirNode,
        fromAstNode: CyanExpression
    ) : FirExpression(parent, fromAstNode) {

        override fun type(): Type {
            val baseExprType = base.type()

            if (!(baseExprType.array || baseExprType == Type.Primitive(CyanType.Str))) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "left-hand side of array index expression must refer to an array",
                        astNode = fromAstNode
                    )
                )
            }

            return baseExprType.asNonArrayType()
        }

        override fun allReferredSymbols() = base.allReferredSymbols()

    }

    class FunctionCall (
        parent: FirNode,
        fromAstNode: CyanFunctionCall,
    ) : FirExpression(parent, fromAstNode), FirStatement {

        lateinit var callee: FirResolvedReference

        var receiver: FirExpression? = null

        val args = ArgumentsHolder(this, fromAstNode)

        override fun type(): Type {
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

            return firFunctionDeclaration.returnType
        }

        override fun allReferredSymbols() = setOf(callee) + args.flatMap { it.allReferredSymbols() }

        class ArgumentsHolder (
            parent: FirNode,
            fromAstNode: CyanExpression,
            val args: MutableList<FirExpression> = mutableListOf()
        ): FirExpression(parent, fromAstNode), MutableList<FirExpression> by args {

            override fun type() = Type.Primitive(CyanType.Any)

            override fun allReferredSymbols() = args.flatMapTo(mutableSetOf()) { it.allReferredSymbols() }

        }

    }

    /**
     * Contains the inline expression if an expression was inlined to replace this expression.
     */
    var inlinedExpr: FirExpression? = null

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
