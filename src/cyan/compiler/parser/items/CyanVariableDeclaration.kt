package cyan.compiler.parser.items

import cyan.compiler.parser.items.expression.CyanExpression
import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression

data class CyanVariableDeclaration(val name: CyanReferenceExpression, val value: CyanExpression?): CyanStatement {
    override fun toString() = "let $name = $value"
}
