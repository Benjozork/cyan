package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanFunctionArgument (
    val name: String,
    val typeAnnotation: CyanTypeAnnotation,
    override val span: Span
): CyanItem {
    override fun toString() = "$name: $typeAnnotation"
}
