package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanFunctionSignature (
    val attributes: List<CyanFunctionAttribute> = emptyList(),
    val receiver: CyanFunctionReceiver? = null,
    val name: CyanIdentifierExpression,
    val args: List<CyanFunctionArgument>,
    val typeAnnotation: CyanTypeAnnotation = CyanTypeAnnotation.Literal(Type.Primitive(CyanType.Void, false), Span(0, 0..0, emptyArray())),
    val isExtern: Boolean,
    override val span: Span? = null
): CyanStatement {

    override fun toString() =
            (if (attributes.isNotEmpty()) attributes.joinToString(", ", "[", "]\n") else "") +
            (if (isExtern) "extern " else "") +
            "function " +
            "${receiver ?: ""}$name(${args.joinToString(", ")})" +
            ": $typeAnnotation"

}
