package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.wasm.lower.WasmExpressionLower
import cyan.compiler.codegen.wasm.lower.WasmFunctionDeclarationLower
import cyan.compiler.codegen.wasm.lower.WasmStatementLower

@Suppress("UNCHECKED_CAST")
class WasmCompilerBackend : FirCompilerBackend() {

    override val prelude  = ""
    override val postlude = ""

    override val statementLower           = WasmStatementLower
    override val expressionLower          = WasmExpressionLower
    override val functionDeclarationLower = WasmFunctionDeclarationLower

    val heap = ByteArray(64) // 64 B heap

    var heapEndPtr = 0x00

    fun alloc(bytes: ByteArray): Int {
        val heapBaseAddr = heapEndPtr

        if (heap.size - bytes.size < heapBaseAddr)
            error("no more space to allocate")

        var addr = heapBaseAddr
        for (byte in bytes) {
            heap[addr] = byte
            addr++
        }

        heapEndPtr = addr + 1
        return heapBaseAddr
    }

    override fun nameForBuiltin(builtinName: String): String {
        TODO("Not yet implemented")
    }

}
