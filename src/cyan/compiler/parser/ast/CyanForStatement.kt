package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanForStatement (
    val variableName: CyanIdentifierExpression,
    val iteratorExpr: CyanExpression,
    val source: CyanSource,
    override val span: Span? = null
) : CyanStatement {

    override fun toString() = """
        for $variableName of $iteratorExpr { ... }
    """.trimIndent()

}
