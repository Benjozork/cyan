package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.operator.*

import kotlin.math.pow

object ConstantFoldingPass : FirOptimizationPass {

    /**
     * Simplifies expressions when possible
     */
    private fun simplify(expression: FirExpression): FirExpression {
        return when (expression) {
            is FirExpression.Binary -> {
                val lhs = expression.lhs
                val op = expression.operator
                val rhs = expression.rhs

               when {
                    lhs is FirExpression.Literal.Boolean && Type.Primitive(CyanType.Bool, false) accepts rhs.type() -> {
                        val simplified = when (op) {
                            CyanBinaryAndOperator -> if (!lhs.value) FirExpression.Literal.Boolean(false, expression.parent, expression.fromAstNode) else rhs
                            CyanBinaryOrOperator -> if (lhs.value) FirExpression.Literal.Boolean(true, expression.parent, expression.fromAstNode) else rhs
                            else -> error("cannot simplify binary boolean expression with operator '${op::class.simpleName}'")
                        }

                        simplified
                    }
                    else -> expression
                }
            }
            else -> expression
        }
    }

    /**
     * Evaluates constant expressions
     */
    private fun evaluate(expr: FirExpression): FirExpression {
        return when (val expression = expr.realExpr) {
            is FirExpression.Literal -> expression
            is FirExpression.Binary -> {
                val lhs = expression.lhs
                val op = expression.operator
                val rhs = expression.rhs

                val (lhsEvaluated, rhsEvaluated) = evaluate(lhs) to evaluate(rhs)

                val expressionParent = expression.parent

                when {
                    lhsEvaluated is FirExpression.Literal.Number && rhsEvaluated is FirExpression.Literal.Number -> {
                        val intValue = when (op) {
                            CyanBinaryPlusOperator  -> lhsEvaluated.value + rhsEvaluated.value
                            CyanBinaryMinusOperator -> lhsEvaluated.value - rhsEvaluated.value
                            CyanBinaryTimesOperator -> lhsEvaluated.value * rhsEvaluated.value
                            CyanBinaryDivOperator   -> lhsEvaluated.value / rhsEvaluated.value
                            CyanBinaryModOperator   -> lhsEvaluated.value % rhsEvaluated.value
                            CyanBinaryExpOperator   -> lhsEvaluated.value.toDouble().pow(rhsEvaluated.value.toDouble()).toInt()
                            else -> null
                        }

                        if (intValue != null) return FirExpression.Literal.Number(intValue, expressionParent, expression.fromAstNode)

                        val booleanValue = when (op) {
                            CyanBinaryLesserOperator        -> lhsEvaluated.value < rhsEvaluated.value
                            CyanBinaryLesserEqualsOperator  -> lhsEvaluated.value <= rhsEvaluated.value
                            CyanBinaryGreaterOperator       -> lhsEvaluated.value > rhsEvaluated.value
                            CyanBinaryGreaterEqualsOperator -> lhsEvaluated.value >= rhsEvaluated.value
                            else -> null
                        }

                        if (booleanValue != null) return FirExpression.Literal.Boolean(booleanValue, expressionParent, expression.fromAstNode)

                        error("could not evaluate with binary operator '${op::class.simpleName}'")
                    }
                    lhsEvaluated is FirExpression.Literal.Boolean && rhsEvaluated is FirExpression.Literal.Boolean -> {
                        return FirExpression.Literal.Boolean(when (op) {
                            CyanBinaryOrOperator  -> lhsEvaluated.value || rhsEvaluated.value
                            CyanBinaryAndOperator -> lhsEvaluated.value && rhsEvaluated.value
                            else -> error("could not evaluate with binary operator '${op::class.simpleName}'")
                        }, expressionParent, expression.fromAstNode)
                    }
                    else -> error("cannot evaluate with binary operands of type '${lhsEvaluated::class.simpleName}' and '${rhsEvaluated::class.simpleName}'")
                }
            }
            is FirExpression.ArrayIndex -> {
                val base = evaluate(expression.base)
                val index = evaluate(expression.index)

                require (base is FirExpression.Literal.Array) { "base was not an array" }
                require (index is FirExpression.Literal.Number) { "index was not a number" }

                base.elements[index.value]
            }
            else -> error("cannot evaluate expression of type '${expression::class.simpleName}'")
        }
    }

    override fun run(source: FirSource) {
        val allVariables = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>()

        allVariables.forEach { it.initializationExpr = simplify(it.initializationExpr) }
        allVariables.filter { it.initializationExpr.realExpr.isConstant }.forEach { it.initializationExpr = evaluate(it.initializationExpr) }

        val allIfChains = source.statements.filterIsInstance<FirIfChain>()

        allIfChains.forEach {
            it.branches
                    .forEach { b -> b.first.inlinedExpr = simplify(b.first.realExpr) }
            it.branches
                    .filter { b -> b.first.isConstant }
                    .forEach { b -> b.first.inlinedExpr = evaluate(b.first.realExpr) }
        }
    }

}
