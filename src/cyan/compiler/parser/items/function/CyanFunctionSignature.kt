package cyan.compiler.parser.items.function

import cyan.compiler.parser.items.expression.CyanIdentifierExpression
import cyan.compiler.parser.items.CyanStatement

class CyanFunctionSignature (
    val name: CyanIdentifierExpression,
    val args: List<CyanIdentifierExpression>
): CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")})"
}
