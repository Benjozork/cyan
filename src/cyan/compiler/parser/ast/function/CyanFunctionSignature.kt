package cyan.compiler.parser.ast.function

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanFunctionSignature (
    val name: CyanIdentifierExpression,
    val args: List<CyanFunctionArgument>,
    val typeAnnotation: CyanTypeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void, false)),
    val isExtern: Boolean
): CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")})" + ": $typeAnnotation"
}
