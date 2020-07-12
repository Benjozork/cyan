package cyan.compiler.parser.items.expression.literal

import cyan.compiler.parser.items.expression.CyanExpression

data class CyanReferenceExpression(val value: String): CyanExpression {
    override fun toString() = value
}
