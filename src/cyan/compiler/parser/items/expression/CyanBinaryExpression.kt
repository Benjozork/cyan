package cyan.compiler.parser.items.expression

import cyan.compiler.parser.items.operator.CyanBinaryOperator

data class CyanBinaryExpression(val lhs: CyanExpression, val operator: CyanBinaryOperator, val rhs: CyanExpression) : CyanExpression
