package cyan.compiler.parser.ast.function

import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.parser.ast.CyanStatement

class CyanFunctionDeclaration (
    val signature: CyanFunctionSignature,
    val source: CyanSource?
): CyanStatement {
    override fun toString() = (if (signature.isExtern) "extern " else "") + "function $signature" + (if (signature.isExtern) "" else "{ ... }")
}
