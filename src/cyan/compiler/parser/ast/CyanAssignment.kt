package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanAssignment(val reference: CyanIdentifierExpression, val newExpr: CyanExpression, override val span: Span? = null): CyanStatement {
    override fun toString() = "$reference = $newExpr"
}
