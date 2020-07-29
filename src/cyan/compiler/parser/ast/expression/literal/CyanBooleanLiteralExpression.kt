package cyan.compiler.parser.ast.expression.literal

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanBooleanLiteralExpression(val value: Boolean, override val span: Span): CyanExpression {
    override fun toString() = value.toString()
}
