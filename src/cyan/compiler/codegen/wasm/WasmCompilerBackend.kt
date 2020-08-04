package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.wasm.lower.WasmExpressionLower
import cyan.compiler.codegen.wasm.lower.WasmFunctionDeclarationLower
import cyan.compiler.codegen.wasm.lower.WasmStatementLower
import cyan.compiler.codegen.wasm.utils.Allocator
import cyan.compiler.fir.FirSource

@Suppress("UNCHECKED_CAST")
class WasmCompilerBackend : FirCompilerBackend() {

    private val d = "$"
    private val n = "\n"

    override val prelude = """
        (import "wasi_unstable" "fd_write" (func ${d}fd_write (param i32 i32 i32 i32) (result i32)))
        
        (memory 1)
        
        (export "memory" (memory 0))
        
        (func ${d}cy_array_check_idx (param ${d}arr_ptr i32) (param ${d}idx i32) (result i32)
            get_local ${d}arr_ptr
            i32.load
            get_local ${d}idx
            i32.le_u
        )

        (func ${d}cy_array_get_i32 (param ${d}arr_ptr i32) (param ${d}arr_idx i32) (result i32)
            (block ${d}B0
                (call ${d}cy_array_check_idx (local.get ${d}arr_ptr) (local.get ${d}arr_idx))
                br_if ${d}B0
            )
            local.get ${d}arr_idx
            i32.const 1
            i32.add
            i32.const 4
            i32.mul
            local.get ${d}arr_ptr
            i32.add
            i32.load
        )
        
        (func ${d}print (param i32)
            (call ${d}fd_write
                (i32.const 1)
                (local.get 0)
                (i32.const 1)
                (i32.const 20)
            )
        
            drop
        )$n
    """.trimIndent()

    override val postlude get() = """
        (data (i32.const 0) "${heapToByteStr()}")
    """.trimIndent()

    override val loweringContext = WasmLoweringContext(this)

    override val statementLower           = WasmStatementLower
    override val expressionLower          = WasmExpressionLower
    override val functionDeclarationLower = WasmFunctionDeclarationLower

    fun generateStartSymbol(source: FirSource): String {
        return "(func \$main (export \"_start\")\n" + source.statements.joinToString("\n", postfix = "\n") { lowerStatement(it, loweringContext).prependIndent("    ") } + ")"
    }

    val allocator = Allocator()

    private fun heapToByteStr(): String {
        fun Byte.toUnsigned(): Int = if (this < 0) this + 127 else this.toInt()
        fun Int.padded(): String = if (this.toString(16).length == 1) "0${this.toString(16)}" else this.toString(16)

        return allocator.heap.joinToString("") { "\\${it.toUnsigned().padded()}" }
    }

    override fun nameForBuiltin(builtinName: String): String {
        TODO("Not yet implemented")
    }

}
