package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanFunctionAttribute (
    val ident: CyanIdentifierExpression,
    override val span: Span? = null
) : CyanItem {

    override fun toString() = ident.value

}
