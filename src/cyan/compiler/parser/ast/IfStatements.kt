package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanExpression

class CyanIfStatement (
    val conditionExpr: CyanExpression,
    val block:         CyanSource
) : CyanStatement {
    override fun toString() = "if ($conditionExpr) { ... }"
}

class CyanIfChain (
    val ifStatements:  Array<CyanIfStatement>,
    val elseBlock: CyanSource?
) : CyanStatement {
    override fun toString() = "${ifStatements.joinToString(", ")}, else: ${elseBlock != null}"
}
