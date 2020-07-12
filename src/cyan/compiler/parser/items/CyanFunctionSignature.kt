package cyan.compiler.parser.items

import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression

class CyanFunctionSignature (
    val name: CyanReferenceExpression,
    val args: List<CyanReferenceExpression>
) : CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")}"
}
