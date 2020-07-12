package cyan.interpreter.evaluator

import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.ast.operator.CyanBinaryPlusOperator
import cyan.interpreter.stack.StackFrame
import cyan.interpreter.resolver.Resolver
import cyan.interpreter.iprintln

fun evaluate(expression: CyanExpression, stackFrame: StackFrame): CyanValue<out Any> {
    iprintln("evaluating ${expression::class.simpleName} { $expression }")

    return when (expression) {
        is CyanIdentifierExpression     -> Resolver.findByIdentifier(expression, stackFrame)
        is CyanNumericLiteralExpression -> CyanNumberValue(expression.value)
        is CyanStringLiteralExpression  -> CyanStringValue(expression.value)
        is CyanBinaryExpression -> {
            val (lhs, op, rhs) = expression

            if (lhs is CyanNumericLiteralExpression && rhs is CyanNumericLiteralExpression) { // Fast path for numeric values
                return CyanNumberValue(when (op) {
                    is CyanBinaryPlusOperator -> lhs.value + rhs.value
                    is CyanBinaryMinusOperator -> lhs.value - rhs.value
                    else -> error("unknown operator ${op::class.simpleName}")
                })
            }

            when (op) { // When lhs or rhs are not literals
                is CyanBinaryPlusOperator -> {
                    val evaluated = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)
                    val (lhsV, rhsV) = evaluated.first.value to evaluated.second.value

                    if (lhsV is Int && rhsV is Int)
                        CyanNumberValue(lhsV + rhsV)
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                is CyanBinaryMinusOperator -> {
                    val evaluated = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)
                    val (lhsV, rhsV) = evaluated.first.value to evaluated.second.value

                    if (lhsV is Int && rhsV is Int)
                        CyanNumberValue(lhsV - rhsV)
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                else -> error("unknown binary expression type ${expression::class.simpleName}")
            }
        }
        is CyanArrayExpression -> CyanArrayValue(expression.exprs.map { evaluate(it, stackFrame) }.toTypedArray())
        else -> error("unknown expression type ${expression::class.simpleName}")
    }
}
