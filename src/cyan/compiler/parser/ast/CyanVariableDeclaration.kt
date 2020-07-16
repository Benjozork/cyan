package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

data class CyanVariableDeclaration(val name: CyanIdentifierExpression, val type: CyanTypeAnnotation?, val value: CyanExpression): CyanStatement {
    override fun toString() = "let $name${if (type != null) ": $type" else ""} = $value"
}
