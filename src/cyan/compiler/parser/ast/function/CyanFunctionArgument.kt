package cyan.compiler.parser.ast.function

import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.CyanTypeAnnotation

class CyanFunctionArgument (
    val name: String,
    val typeAnnotation: CyanTypeAnnotation
): CyanItem {
    override fun toString() = "$name: $typeAnnotation"
}
