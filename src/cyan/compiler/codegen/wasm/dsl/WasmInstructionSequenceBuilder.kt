package cyan.compiler.codegen.wasm.dsl

class WasmInstructionSequenceBuilder : WasmBlock {

    override val elements = mutableListOf<Wasm.OrderedElement>()

    override fun toString() = """
    |${elements.joinToString("\n")}
    """.trimMargin()

}
