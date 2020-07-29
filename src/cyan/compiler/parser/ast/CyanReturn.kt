package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

class CyanReturn(val expr: CyanExpression, override val span: Span? = null): CyanStatement {
    override fun toString() = "return $expr"
}
