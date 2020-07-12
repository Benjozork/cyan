package cyan.compiler.parser.items.function

import cyan.compiler.parser.items.CyanSource
import cyan.compiler.parser.items.CyanStatement

class CyanFunctionDeclaration (
    val signature: CyanFunctionSignature,
    val source: CyanSource
): CyanStatement {
    override fun toString() = "$signature { ... }"
}
