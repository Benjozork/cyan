package cyan.compiler.parser.ast

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

data class CyanVariableDeclaration (
    val name: CyanIdentifierExpression,
    val mutable: Boolean,
    val type: Type?,
    val value: CyanExpression
): CyanStatement {
    override fun toString() = "let $name${if (type != null) ": $type" else ""} = $value"
}
