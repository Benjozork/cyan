package cyan.compiler.codegen.wasm.dsl

class WasmInstructionSequence : WasmScope {

    override val elements = mutableListOf<Wasm.OrderedElement>()

    override fun toString() = """
    |${elements.joinToString("\n")}
    """.trimMargin()

}
