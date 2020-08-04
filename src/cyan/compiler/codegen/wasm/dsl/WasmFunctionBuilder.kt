package cyan.compiler.codegen.wasm.dsl

class WasmFunctionBuilder(private val name: String, private val parameters: MutableList<Parameter> = mutableListOf(), private val exportedAs: String? = null) : WasmBlock {

    class Parameter(val name: String, val type: String) {

        override fun toString() = "(param \$$name $type)"

        companion object {

            @WasmInstructionsBuilderDsl
            fun param(name: String, type: String) = Parameter(name, type)

        }

    }

    override val elements = mutableListOf<Wasm.OrderedElement>()

    override fun toString(): String {
        var body = ""

        val localDeclarations = this.elements.filter { it is Wasm.Instruction && it.text.startsWith("(local ") }
        val nonLocalDeclarations = this.elements - localDeclarations

        for ((index, local) in localDeclarations.withIndex()) {
            body += (if (index > 0) "\n" else "") + local.toString().prependIndent("    ")
        }

        for ((index, element) in nonLocalDeclarations.withIndex()) {
            if (index == 0 && localDeclarations.isNotEmpty())
                body += "\n"
            body += (if (index > 0) "\n" else "") + element.toString().prependIndent("    ")
        }

        val exportString = if (exportedAs != null) " (export \"$exportedAs\") " else ""

        return """
        |(func ${"$"}$name$exportString${parameters.joinToString(" ", prefix = " ")}
        |$body
        |)
        """.trimMargin()
    }

}


