package cyan.compiler.parser.ast.expression.literal

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanNumericLiteralExpression(val value: Int, override val span: Span? = null): CyanExpression {
    override fun toString() = value.toString()
}
