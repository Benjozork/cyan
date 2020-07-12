package cyan.compiler.parser.items

import cyan.compiler.parser.items.expression.CyanExpression
import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression

data class CyanFunctionCall(val functionName: CyanReferenceExpression, val args: Array<CyanExpression>): CyanStatement
