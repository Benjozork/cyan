package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanExpression

class CyanWhileStatement(val conditionExpr: CyanExpression, val source: CyanSource, override val span: Span) : CyanStatement {

    override fun toString() = """
        while $conditionExpr { ... }
    """.trimIndent()

}
