package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span

data class CyanIdentifierExpression(val value: String, override val span: Span? = null): CyanExpression {

    override fun toString() = value

}
