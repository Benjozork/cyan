package cyan.compiler.parser.items

class CyanFunctionDeclaration (
    val signature: CyanFunctionSignature,
    val source:    CyanSource
) : CyanStatement {
    override fun toString() = "$signature { ... }"
}
