package cyan.compiler.codegen.wasm.dsl

class WasmFunction (
    private val name: String,
    private val parameters: MutableList<Parameter> = mutableListOf(),
    private val returnType: Wasm.Type?,
    private val exportedAs: String? = null
) : WasmScope {

    class Parameter(val name: String, val type: Wasm.Type) {

        override fun toString() = "(param \$$name $type)"

        companion object {

            @WasmInstructionsBuilderDsl
            fun param(name: String, type: Wasm.Type) = Parameter(name, type)

        }

    }

    override val elements = mutableListOf<Wasm.OrderedElement>()

    override fun toString(): String {
        var body = ""

        var flattenedElements = this.elements.toList()

        while (flattenedElements.any { it is WasmInstructionSequence }) {
            flattenedElements = flattenedElements.flatMap { if (it is WasmInstructionSequence) it.elements else listOf(it) }
        }

        val localDeclarations = flattenedElements.filterIsInstance<Wasm.Local>()
        val nonLocalDeclarations = flattenedElements - localDeclarations

        for ((index, local) in localDeclarations.withIndex()) {
            body += (if (index > 0) "\n" else "") + local.toString().prependIndent("    ")
        }

        for ((index, element) in nonLocalDeclarations.withIndex()) {
            if (index == 0 && localDeclarations.isNotEmpty())
                body += "\n"
            body += (if (index > 0) "\n" else "") + element.toString().prependIndent("    ")
        }

        val exportString = if (exportedAs != null) " (export \"$exportedAs\") " else ""
        val returnTypeString = if (returnType != null) " (result $returnType) " else ""

        return """
        |(func ${"$"}$name$exportString${parameters.joinToString(" ")}$returnTypeString
        |$body
        |)
        """.trimMargin()
    }

}


