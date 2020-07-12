package cyan.compiler.parser.items.function

import cyan.compiler.parser.items.CyanStatement
import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression

class CyanFunctionSignature (
    val name: CyanReferenceExpression,
    val args: List<CyanReferenceExpression>
): CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")})"
}
