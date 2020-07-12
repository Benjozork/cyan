package cyan.compiler.parser.ast.expression.literal

import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanNumericLiteralExpression(val value: Int): CyanExpression {
    override fun toString() = value.toString()
}
