package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanExpression

class CyanIfStatement (
    val conditionExpr: CyanExpression,
    val block:         CyanSource
) : CyanStatement {
    override fun toString() = "if ($conditionExpr) { ... }"
}
