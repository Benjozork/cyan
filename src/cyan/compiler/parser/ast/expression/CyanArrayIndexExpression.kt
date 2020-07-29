package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression

class CyanArrayIndexExpression(val base: CyanExpression, val index: CyanNumericLiteralExpression, override val span: Span? = null) : CyanExpression {
    override fun toString() = "$base[$index]"
}
