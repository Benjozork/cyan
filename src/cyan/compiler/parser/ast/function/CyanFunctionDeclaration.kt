package cyan.compiler.parser.ast.function

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.parser.ast.CyanStatement

class CyanFunctionDeclaration (
    val signature: CyanFunctionSignature,
    val source: CyanSource?,
    override val span: Span? = null
): CyanStatement {

    override fun toString() = "$signature { ... }"

}
