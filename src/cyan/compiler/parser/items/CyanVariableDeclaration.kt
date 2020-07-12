package cyan.compiler.parser.items

import cyan.compiler.parser.items.expression.CyanExpression
import cyan.compiler.parser.items.expression.CyanIdentifierExpression

data class CyanVariableDeclaration(val name: CyanIdentifierExpression, val value: CyanExpression?): CyanStatement {
    override fun toString() = "let $name = $value"
}
