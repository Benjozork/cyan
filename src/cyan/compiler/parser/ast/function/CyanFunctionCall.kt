package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

data class CyanFunctionCall (
    val base: CyanExpression,
    val args: Array<Argument>,
    override val span: Span? = null
): CyanStatement, CyanExpression {

    data class Argument(val label: CyanIdentifierExpression?, val value: CyanExpression, val span: Span? = null) {

        override fun toString() = if (label != null) "$label: $value" else "$value"

    }

    override fun toString() = "$base(${args.joinToString(", ")})"

}
