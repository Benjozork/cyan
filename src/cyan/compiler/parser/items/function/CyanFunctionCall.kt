package cyan.compiler.parser.items.function

import cyan.compiler.parser.items.expression.CyanIdentifierExpression
import cyan.compiler.parser.items.CyanStatement
import cyan.compiler.parser.items.expression.CyanExpression

data class CyanFunctionCall(val functionName: CyanIdentifierExpression, val args: Array<CyanExpression>): CyanStatement {
    override fun toString() = "$functionName(${args.joinToString(", ")})"
}
