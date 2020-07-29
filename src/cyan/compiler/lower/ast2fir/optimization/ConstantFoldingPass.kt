package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.*

import kotlin.math.pow

object ConstantFoldingPass : FirOptimizationPass {

    /**
     * Simplifies expressions when possible
     */
    private fun simplify(expression: FirExpression): FirExpression {
        return when (val expr = expression.astExpr) {
            is CyanBinaryExpression -> {
                val (lhs, op, rhs) = expr

                when {
                    lhs is CyanBooleanLiteralExpression && Type.Primitive(CyanType.Bool, false) accepts expression.makeChildExpr(rhs).type() -> {
                        val simplified = expression.parent.makeChildExpr(when (op) {
                            CyanBinaryAndOperator -> if (!lhs.value) CyanBooleanLiteralExpression(false) else rhs
                            CyanBinaryOrOperator -> if (lhs.value) CyanBooleanLiteralExpression(true) else rhs
                            else -> error("cannot simplify binary boolean expression with operator '${op::class.simpleName}'")
                        })

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
    private fun evaluate(expression: FirExpression): FirExpression {
        return when (val expr = expression.astExpr) {
            is CyanNumericLiteralExpression,
            is CyanStringLiteralExpression,
            is CyanBooleanLiteralExpression -> expression
            is CyanBinaryExpression -> {
                val (lhs, op, rhs) = expr

                val (lhsEvaluated, rhsEvaluated) = evaluate(expression.makeChildExpr(lhs)).astExpr to evaluate(expression.makeChildExpr(rhs)).astExpr

                val expressionParent = expression.parent

                when {
                    lhsEvaluated is CyanNumericLiteralExpression && rhsEvaluated is CyanNumericLiteralExpression -> {
                        val intValue = when (op) {
                            CyanBinaryPlusOperator  -> lhsEvaluated.value + rhsEvaluated.value
                            CyanBinaryMinusOperator -> lhsEvaluated.value - rhsEvaluated.value
                            CyanBinaryTimesOperator -> lhsEvaluated.value * rhsEvaluated.value
                            CyanBinaryDivOperator   -> lhsEvaluated.value / rhsEvaluated.value
                            CyanBinaryModOperator   -> lhsEvaluated.value % rhsEvaluated.value
                            CyanBinaryExpOperator   -> lhsEvaluated.value.toDouble().pow(rhsEvaluated.value.toDouble()).toInt()
                            else -> null
                        }

                        if (intValue != null) return expressionParent.makeChildExpr(CyanNumericLiteralExpression(intValue))

                        val booleanValue = when (op) {
                            CyanBinaryLesserOperator -> lhsEvaluated.value < rhsEvaluated.value
                            else -> null
                        }

                        if (booleanValue != null) return expressionParent.makeChildExpr(CyanBooleanLiteralExpression(booleanValue))

                        error("could not evaluate with binary operator '${op::class.simpleName}'")
                    }
                    lhsEvaluated is CyanBooleanLiteralExpression && rhsEvaluated is CyanBooleanLiteralExpression -> {
                        return expressionParent.makeChildExpr(CyanBooleanLiteralExpression(when (op) {
                            CyanBinaryOrOperator  -> lhsEvaluated.value || rhsEvaluated.value
                            CyanBinaryAndOperator -> lhsEvaluated.value && rhsEvaluated.value
                            else -> error("could not evaluate with binary operator '${op::class.simpleName}'")
                        }))
                    }
                    else -> error("cannot evaluate with binary operands of type '${lhsEvaluated::class.simpleName}' and '${rhsEvaluated::class.simpleName}'")
                }
            }
            else -> error("cannot evaluate expression of type '${expr::class.simpleName}'")
        }
    }

    override fun run(source: FirSource) {
        val allVariables = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>()

        allVariables.forEach { it.initializationExpr = simplify(it.initializationExpr) }
        allVariables.filter { it.initializationExpr.isConstant }.forEach { it.initializationExpr = evaluate(it.initializationExpr) }

        val allIfChains = source.statements.filterIsInstance<FirIfChain>()

        allIfChains.forEach { it.branches = it.branches.map { b ->  simplify(b.first) to b.second } }
    }

}
