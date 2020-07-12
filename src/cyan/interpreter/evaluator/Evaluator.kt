package cyan.interpreter.evaluator

import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.ast.operator.CyanBinaryPlusOperator
import cyan.interpreter.stack.StackFrame
import cyan.interpreter.iprintln

fun evaluate(expression: CyanExpression, stackFrame: StackFrame): Any? {
    iprintln("evaluating ${expression::class.simpleName} { $expression }")

    return when (expression) {
        is CyanIdentifierExpression     -> stackFrame.localVariables[expression.value]
        is CyanNumericLiteralExpression -> expression.value
        is CyanStringLiteralExpression  -> expression.value
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
