package cyan.interpreter.evaluator

import cyan.compiler.parser.items.expression.CyanBinaryExpression
import cyan.compiler.parser.items.expression.CyanExpression
import cyan.compiler.parser.items.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression
import cyan.compiler.parser.items.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.items.operator.CyanBinaryPlusOperator
import cyan.interpreter.stack.StackFrame
import cyan.interpreter.iprintln

fun evaluate(expression: CyanExpression, stackFrame: StackFrame): Any {
    iprintln("evaluating ${expression::class.simpleName} { $expression }")

    return when (expression) {
        is CyanNumericLiteralExpression -> expression.value
        is CyanReferenceExpression      -> expression.value
        is CyanBinaryExpression -> {
            val (lhs, op, rhs) = expression

            when (op) {
                is CyanBinaryPlusOperator -> {
                    val evaluated = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (evaluated.first is Int && evaluated.second is Int)
                        evaluated.first as Int + evaluated.second as Int
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                is CyanBinaryMinusOperator -> {
                    val evaluated = evaluate(lhs, stackFrame) to evaluate(rhs, stackFrame)

                    if (evaluated.first is Int && evaluated.second is Int)
                        evaluated.first as Int - evaluated.second as Int
                    else error("can't run plus operation on ${lhs::class.simpleName} and ${rhs::class.simpleName}")
                }
                else -> error("unknown binary expression type ${expression::class.simpleName}")
            }
        }
        else -> error("unknown expression type ${expression::class.simpleName}")
    }
}
