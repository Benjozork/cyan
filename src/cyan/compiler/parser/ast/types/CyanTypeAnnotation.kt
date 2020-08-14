package cyan.compiler.parser.ast.types

import cyan.compiler.common.Span
import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

sealed class CyanTypeAnnotation(override val span: Span? = null) : CyanItem {

    class Literal(val literalType: Type.Primitive, span: Span? = null): CyanTypeAnnotation(span) {
        override fun toString() = literalType.toString()
    }

    class Reference(val identifierExpression: CyanIdentifierExpression, span: Span? = null): CyanTypeAnnotation(span) {
        override fun toString() = identifierExpression.value
    }

}
