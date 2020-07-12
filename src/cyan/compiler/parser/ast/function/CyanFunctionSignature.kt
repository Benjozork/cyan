package cyan.compiler.parser.ast.function

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement

class CyanFunctionSignature (
    val name: CyanIdentifierExpression,
    val args: List<CyanIdentifierExpression>
): CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")})"
}
