package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span

class CyanArrayIndexExpression(val base: CyanExpression, val index: CyanExpression, override val span: Span? = null) : CyanExpression {
    override fun toString() = "$base[$index]"
}
