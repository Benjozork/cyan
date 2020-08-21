package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

class CyanAssignment(val base: CyanExpression, val newExpr: CyanExpression, override val span: Span? = null): CyanStatement {
    override fun toString() = "$base = $newExpr"
}
