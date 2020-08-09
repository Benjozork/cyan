package cyan.compiler.codegen.wasm.dsl

@Suppress("FunctionName", "PropertyName")
interface WasmScope : Wasm.OrderedElement {

    val elements: MutableList<out Wasm.OrderedElement>

    override fun toString(): String

    @Suppress("UNCHECKED_CAST")
    fun pushElement(element: Wasm.OrderedElement) = (elements as MutableList<Wasm.OrderedElement>).add(element)

    @WasmInstructionsBuilderDsl
    fun Int32Instructions.const(value: Int) =
            pushElement(Wasm.Instruction("i32.const $value"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.load get() =
        pushElement(Wasm.Instruction("i32.load"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.store get() =
        pushElement(Wasm.Instruction("i32.store"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.add get() =
        pushElement(Wasm.Instruction("i32.add"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.sub get() =
        pushElement(Wasm.Instruction("i32.sub"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.mul get() =
        pushElement(Wasm.Instruction("i32.mul"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.lt_u get() =
        pushElement(Wasm.Instruction("i32.lt_u"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.le_u get() =
            pushElement(Wasm.Instruction("i32.le_u"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.gt_u get() =
        pushElement(Wasm.Instruction("i32.gt_u"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.ge_u get() =
        pushElement(Wasm.Instruction("i32.ge_u"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.eqz get() =
        pushElement(Wasm.Instruction("i32.eqz"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.get() =
        pushElement(Wasm.Instruction("local.get"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.get(numLocal: Int) =
        pushElement(Wasm.Instruction("local.get \$$numLocal"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.get(local: String) =
            pushElement(Wasm.Instruction("local.get \$$local"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.set(numLocal: Int) =
            pushElement(Wasm.Instruction("local.set \$$numLocal"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.set(local: String) =
            pushElement(Wasm.Instruction("local.set \$$local"))


    @WasmInstructionsBuilderDsl
    fun LocalInstructions.new(numLocal: Int, type: Wasm.Type) =
            pushElement(Wasm.Local(numLocal.toString(), type))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.new(local: String, type: Wasm.Type) =
            pushElement(Wasm.Local(local, type))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.tee() =
            pushElement(Wasm.Instruction("local.tee"))

    @WasmInstructionsBuilderDsl
    fun LocalInstructions.tee(numLocal: Int, value: Int) =
            pushElement(Wasm.Instruction("(local.tee \$$numLocal ${i32.const(value)})"))

    @WasmInstructionsBuilderDsl
    fun CyanIntrinsics.malloc(size: Int) =
            pushElement(Wasm.Instruction("call \$cy_malloc"))

    @WasmInstructionsBuilderDsl
    fun br(blockNum: Int): Boolean {
        val prefix = if (this is WasmLoop) "L" else "B"
        return pushElement(Wasm.Instruction("br \$$prefix$blockNum"))
    }

    @WasmInstructionsBuilderDsl
    fun br_if(blockNum: Int): Boolean {
        val prefix = if (this is WasmLoop) "L" else "B"
        return pushElement(Wasm.Instruction("br_if \$$prefix$blockNum"))
    }

    @WasmInstructionsBuilderDsl
    fun call(funcName: String) =
        pushElement(Wasm.Instruction("call \$$funcName"))

    @WasmInstructionsBuilderDsl
    val drop get() =
            pushElement(Wasm.Instruction("drop"))

    @WasmInstructionsBuilderDsl
    operator fun Wasm.OrderedElement.unaryPlus() = this@WasmScope.pushElement(this)

}
