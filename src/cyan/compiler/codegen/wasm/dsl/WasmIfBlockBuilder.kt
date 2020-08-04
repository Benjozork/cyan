package cyan.compiler.codegen.wasm.dsl

class WasmIfBlockBuilder (
    private val conditionNum: Int,
    private val conditionExpression: WasmInstructionSequenceBuilder
) : Wasm.OrderedElement {

    val ifElements = mutableListOf<Wasm.OrderedElement>()

    val elseElements = mutableListOf<Wasm.OrderedElement>()

    override fun toString(): String {
        var ifBlockElements = ""

        for ((index, element) in ifElements.withIndex()) {
            ifBlockElements += (if (index > 0) "\n" else "") + element.toString().prependIndent("    ")
        }

        var elseBlockElements = ""

        for ((index, element) in elseElements.withIndex()) {
            elseBlockElements += (if (index > 0) "\n" else "") + element.toString().prependIndent("    ")
        }

        return """
        |$conditionExpression
        |if ${"$"}C$conditionNum
        |$ifBlockElements
        |else ${"$"}C$conditionNum
        |$elseBlockElements
        |end
        """.trimMargin()
    }

}
