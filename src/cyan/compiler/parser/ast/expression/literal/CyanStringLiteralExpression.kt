package cyan.compiler.parser.ast.expression.literal

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanStringLiteralExpression(val value: String, override val span: Span? = null): CyanExpression {
    override fun toString() = "\"$value\""
}
