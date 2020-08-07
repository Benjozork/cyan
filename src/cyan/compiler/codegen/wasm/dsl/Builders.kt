package cyan.compiler.codegen.wasm.dsl

@WasmInstructionsBuilderDsl
fun func(funcName: String, vararg parameters: WasmFunction.Parameter, returnType: Wasm.Type?, exportedAs: String? = null, block: WasmFunction.() -> Unit): WasmFunction
        = WasmFunction(funcName, parameters.toMutableList(), returnType, exportedAs).also(block)

@WasmInstructionsBuilderDsl
fun block(blockNum: Int, block: WasmBlock.() -> Unit): WasmBlock
        = WasmBlock(blockNum).also(block)

@WasmInstructionsBuilderDsl
fun loop(blockNum: Int, block: WasmLoop.() -> Unit): WasmLoop
        = WasmLoop(blockNum).also(block)

@WasmInstructionsBuilderDsl
fun condition (
    conditionNum: Int,
    conditionExpression: WasmInstructionSequence,
    ifBlock: WasmIfBlock.() -> Unit,
    otherwise: WasmIfBlock.() -> Unit
): WasmIfBlock
        = WasmIfBlock(conditionNum, conditionExpression).also(ifBlock).also(otherwise)

@WasmInstructionsBuilderDsl
fun instructions(block: WasmInstructionSequence.() -> Unit): WasmInstructionSequence =
    WasmInstructionSequence().also(block)

@WasmInstructionsBuilderDsl
fun WasmScope.block(blockNum: Int, block: WasmBlock.() -> Unit): WasmBlock
        = WasmBlock(blockNum).also(block).also { this.pushElement(it) }

@WasmInstructionsBuilderDsl
fun WasmScope.loop(blockNum: Int, block: WasmLoop.() -> Unit): WasmLoop
        = WasmLoop(blockNum).also(block).also { this.pushElement(it) }

@WasmInstructionsBuilderDsl
fun WasmScope.condition (
    conditionNum: Int,
    conditionExpression: WasmInstructionSequence.() -> Unit,
    ifBlock: WasmIfBlock.() -> Unit,
    elseBlock: WasmIfBlock.() -> Unit
): WasmIfBlock
        = WasmIfBlock(conditionNum, WasmInstructionSequence().also(conditionExpression)).also(ifBlock).also(elseBlock)
