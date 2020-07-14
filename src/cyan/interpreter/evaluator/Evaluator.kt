package cyan.interpreter.evaluator

import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.*
import cyan.interpreter.evaluator.values.*
import cyan.interpreter.ierror
import cyan.interpreter.stack.StackFrame
import cyan.interpreter.resolver.Resolver
import cyan.interpreter.iprintln
import cyan.interpreter.runtime.Builtins

fun evaluate(expression: CyanExpression, stackFrame: StackFrame): CyanValue<out Any> {
    iprintln("evaluating ${expression::class.simpleName} { $expression }")

    return when (expression) {
        is CyanNumericLiteralExpression -> CyanNumberValue(expression.value)
        is CyanStringLiteralExpression  -> CyanStringValue(expression.value)
        is CyanBooleanLiteralExpression -> CyanBooleanValue(expression.value)
        is CyanIdentifierExpression     -> Resolver.findByIdentifier(expression, stackFrame)
        is CyanBinaryExpression -> {
            val (lhs, op, rhs) = expression

            if (lhs is CyanNumericLiteralExpression && rhs is CyanNumericLiteralExpression) { // Fast path for numeric values
                return CyanNumberValue(when (op) {
                    is CyanBinaryPlusOperator -> lhs.value + rhs.value
                    is CyanBinaryMinusOperator -> lhs.value - rhs.value
                    is CyanBinaryTimesOperator -> lhs.value * rhs.value
                    is CyanBinaryDivOperator -> lhs.value / rhs.value
                    is CyanBinaryModOperator -> lhs.value % rhs.value
                    else -> error("unknown operator ${op::class.simpleName}")
                })
            }

            when (op) { // When lhs or rhs are not literals
                is CyanBinaryPlusOperator -> {
                    val (lhsV, rhsV)  = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (lhsV is CyanNumberValue && rhsV is CyanNumberValue)
                        CyanNumberValue(lhsV.value + rhsV.value)
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                is CyanBinaryMinusOperator -> {
                    val (lhsV, rhsV)  = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (lhsV is CyanNumberValue && rhsV is CyanNumberValue)
                        CyanNumberValue(lhsV.value - rhsV.value)
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                is CyanBinaryAndOperator -> {
                    val (lhsV, rhsV) = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (lhsV is CyanBooleanValue && rhsV is CyanBooleanValue) {
                        CyanBooleanValue(lhsV.value && rhsV.value)
                    } else ierror("logical and only possible on two boolean values")
                }
                is CyanBinaryOrOperator -> {
                    val (lhsV, rhsV) = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (lhsV is CyanBooleanValue && rhsV is CyanBooleanValue) {
                        CyanBooleanValue(lhsV.value || rhsV.value)
                    } else ierror("logical or only possible on two boolean values")
                }
                else -> error("unknown binary operator type ${op::class.simpleName}")
            }
        }
        is CyanArrayExpression -> CyanArrayValue(expression.exprs.map { evaluate(it, stackFrame) }.toTypedArray())
        is CyanMemberAccessExpression -> {
            val value = evaluate(expression.base, stackFrame)
            val builtin = Builtins.functions[value::class]?.get(expression.member.value)

            return if (builtin != null) {
                builtin(value)
            } else ierror("no builtin found for '$expression' on value of type ${value::class.simpleName}")
        }
        else -> error("unknown expression type ${expression::class.simpleName}")
    }
}
