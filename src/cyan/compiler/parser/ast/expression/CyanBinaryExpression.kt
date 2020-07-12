package cyan.compiler.parser.ast.expression

import cyan.compiler.parser.ast.operator.CyanBinaryOperator

data class CyanBinaryExpression(val lhs: CyanExpression, val operator: CyanBinaryOperator, val rhs: CyanExpression) : CyanExpression {
    override fun toString() = "$lhs $operator $rhs"
}
