package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanAssignment(val reference: CyanIdentifierExpression, val newExpr: CyanExpression): CyanStatement {
    override fun toString() = "$reference = $newExpr"
}
