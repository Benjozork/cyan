package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span

class CyanMemberAccessExpression(
    val base: CyanExpression,
    val member: CyanIdentifierExpression,
    override val span: Span? = null
) : CyanExpression {
    override fun toString() = "$base.$member"
}
