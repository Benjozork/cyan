package cyan.compiler.codegen.wasm.dsl

@WasmInstructionsBuilderDsl
fun func(funcName: String, vararg parameters: WasmFunctionBuilder.Parameter, exportedAs: String? = null, block: WasmFunctionBuilder.() -> Unit): WasmFunctionBuilder
        = WasmFunctionBuilder(funcName, parameters.toMutableList(), exportedAs).also(block)

@WasmInstructionsBuilderDsl
fun block(blockNum: Int, block: WasmBlockBuilder.() -> Unit): WasmBlockBuilder
        = WasmBlockBuilder(blockNum).also(block)

@WasmInstructionsBuilderDsl
fun loop(blockNum: Int, block: WasmLoopBuilder.() -> Unit): WasmLoopBuilder
        = WasmLoopBuilder(blockNum).also(block)

@WasmInstructionsBuilderDsl
fun condition (
    conditionNum: Int,
    conditionExpression: WasmInstructionSequenceBuilder,
    ifBlockBuilder: WasmIfBlockBuilder.() -> Unit,
    otherwise: WasmIfBlockBuilder.() -> Unit
): WasmIfBlockBuilder
        = WasmIfBlockBuilder(conditionNum, conditionExpression).also(ifBlockBuilder).also(otherwise)

@WasmInstructionsBuilderDsl
fun instructions(block: WasmInstructionSequenceBuilder.() -> Unit): WasmInstructionSequenceBuilder =
    WasmInstructionSequenceBuilder().also(block)

@WasmInstructionsBuilderDsl
fun WasmBlock.block(blockNum: Int, block: WasmBlockBuilder.() -> Unit): WasmBlockBuilder
        = WasmBlockBuilder(blockNum).also(block).also { this.pushElement(it) }

@WasmInstructionsBuilderDsl
fun WasmBlock.loop(blockNum: Int, block: WasmLoopBuilder.() -> Unit): WasmLoopBuilder
        = WasmLoopBuilder(blockNum).also(block).also { this.pushElement(it) }

@WasmInstructionsBuilderDsl
fun WasmBlock.condition (
    conditionNum: Int,
    conditionExpression: WasmInstructionSequenceBuilder.() -> Unit,
    ifBlockBuilder: WasmIfBlockBuilder.() -> Unit,
    elseBlockBuilder: WasmIfBlockBuilder.() -> Unit
): WasmIfBlockBuilder
        = WasmIfBlockBuilder(conditionNum, WasmInstructionSequenceBuilder().also(conditionExpression)).also(ifBlockBuilder).also(elseBlockBuilder)
