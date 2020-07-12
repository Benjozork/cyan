package cyan.compiler.parser.items.expression.literal

import cyan.compiler.parser.items.expression.CyanExpression

data class CyanNumericLiteralExpression(val value: Int): CyanExpression
