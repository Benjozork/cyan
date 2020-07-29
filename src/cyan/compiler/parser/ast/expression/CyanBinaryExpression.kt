package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.operator.CyanBinaryOperator

data class CyanBinaryExpression(val lhs: CyanExpression, val operator: CyanBinaryOperator, val rhs: CyanExpression, override val span: Span) : CyanExpression {
    override fun toString() = "$lhs $operator $rhs"
}
