package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanFunctionReceiver (
    val type: CyanTypeAnnotation,
    override val span: Span? = null
) : CyanItem {

    override fun toString() = "($type)."

}
