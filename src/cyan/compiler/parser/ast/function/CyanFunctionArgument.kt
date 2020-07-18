package cyan.compiler.parser.ast.function

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.CyanItem

class CyanFunctionArgument (
    val name: String,
    val typeAnnotation: Type
): CyanItem {
    override fun toString() = "$name: $typeAnnotation"
}
