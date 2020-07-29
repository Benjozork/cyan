package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanFunctionCall (
    val functionIdentifier: CyanIdentifierExpression,
    val args: Array<CyanExpression>,
    override val span: Span
): CyanStatement, CyanExpression {
    override fun toString() = "$functionIdentifier(${args.joinToString(", ")})"
}
