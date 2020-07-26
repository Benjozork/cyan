package cyan.compiler.parser.ast.function

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.CyanType

class CyanFunctionSignature (
    val name: CyanIdentifierExpression,
    val args: List<CyanFunctionArgument>,
    val typeAnnotation: Type = Type(CyanType.Void, false),
    val isExtern: Boolean
): CyanStatement {
    override fun toString() = "$name(${args.joinToString(", ")})" + ": $typeAnnotation"
}
