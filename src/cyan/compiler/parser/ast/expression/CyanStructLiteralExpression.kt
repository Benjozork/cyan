package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span

class CyanStructLiteralExpression (
    val exprs: Array<CyanExpression>,
    override val span: Span? = null
) : CyanExpression {

    override fun toString() = "{ ${exprs.joinToString(", ")} }"

}
