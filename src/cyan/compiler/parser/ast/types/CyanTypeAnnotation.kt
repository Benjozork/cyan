package cyan.compiler.parser.ast.types

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

sealed class CyanTypeAnnotation : CyanItem {

    class Literal(val literalType: Type.Primitive): CyanTypeAnnotation() {
        override fun toString() = literalType.toString()
    }

    class Reference(val identifierExpression: CyanIdentifierExpression): CyanTypeAnnotation() {
        override fun toString() = identifierExpression.value
    }

}
