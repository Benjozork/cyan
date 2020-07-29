package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

data class CyanVariableDeclaration (
    val name: CyanIdentifierExpression,
    val mutable: Boolean,
    val type: CyanTypeAnnotation?,
    val value: CyanExpression,
    override val span: Span
): CyanStatement {
    override fun toString() = "let $name${if (type != null) ": $type" else ""} = $value"
}
