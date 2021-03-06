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
    val Int32Instructions.div_s get() =
        pushElement(Wasm.Instruction("i32.div_s"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.div_u get() =
        pushElement(Wasm.Instruction("i32.div_u"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.rem_s get() =
        pushElement(Wasm.Instruction("i32.rem_s"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.rem_u get() =
        pushElement(Wasm.Instruction("i32.rem_u"))

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
    val Int32Instructions.eq get() =
        pushElement(Wasm.Instruction("i32.eq"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.ne get() =
        pushElement(Wasm.Instruction("i32.ne"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.and get() =
        pushElement(Wasm.Instruction("i32.and"))

    @WasmInstructionsBuilderDsl
    val Int32Instructions.or get() =
        pushElement(Wasm.Instruction("i32.or"))

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
            pushElement(Wasm.Instruction("(call \$cy_malloc (i32.const $size))"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.array_get get() =
        pushElement(Wasm.Instruction("call \$cy_array_get"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.array_set get() =
        pushElement(Wasm.Instruction("call \$cy_array_set"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strcharat get() =
        pushElement(Wasm.Instruction("call \$cy_str_char_at"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strcharat_as_str get() =
        pushElement(Wasm.Instruction("call \$cy_str_char_at_as_str"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strlen get() =
        pushElement(Wasm.Instruction("call \$cy_str_len"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strcpy get() =
        pushElement(Wasm.Instruction("call \$cy_str_copy"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strcat get() =
        pushElement(Wasm.Instruction("call \$cy_str_cat"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strtoiov get() =
        pushElement(Wasm.Instruction("call \$cy_str_to_iov"))

    @WasmInstructionsBuilderDsl
    val CyanIntrinsics.strcmp get() =
        pushElement(Wasm.Instruction("call \$cy_str_cmp"))

    @WasmInstructionsBuilderDsl
    fun br(blockNum: Int, block: Boolean = false): Boolean {
        val prefix = if (this is WasmLoop && !block) "L" else "B"
        return pushElement(Wasm.Instruction("br \$$prefix$blockNum"))
    }

    @WasmInstructionsBuilderDsl
    fun br_if(blockNum: Int, block: Boolean = false): Boolean {
        val prefix = if (this is WasmLoop && !block) "L" else "B"
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
