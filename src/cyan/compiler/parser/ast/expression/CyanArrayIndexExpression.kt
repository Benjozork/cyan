package cyan.compiler.parser.ast.expression

import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression

class CyanArrayIndexExpression(val base: CyanExpression, val index: CyanNumericLiteralExpression) : CyanExpression {
    override fun toString() = "$base[$index]"
}
