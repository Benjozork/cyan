package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

sealed class CyanFunctionAttribute (
    override val span: Span? = null
) : CyanItem {

    class Keyword(val ident: CyanIdentifierExpression, override val span: Span? = null): CyanFunctionAttribute(span) {

        override fun toString() = ident.value

    }

    class Value(val ident: CyanIdentifierExpression, val value: CyanExpression, override val span: Span? = null): CyanFunctionAttribute(span) {

        override fun toString() = ident.value

    }

}
