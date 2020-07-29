package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span

class CyanArrayExpression(val exprs: Array<CyanExpression>, override val span: Span? = null) : CyanExpression {
    override fun toString() = "[${exprs.joinToString(", ")}]"
}
