package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.wasm.WasmCompilerBackend

import java.nio.ByteBuffer

object WasmStringTransformer {

    fun stringToIov(backend: WasmCompilerBackend, string: String): Int {
        val bytes = string.toByteArray()

        val pointerTarget = backend.allocator.prealloc(bytes)

        val iovPtr = ByteBuffer.allocate(Integer.BYTES).putInt(pointerTarget).array().reversed().toByteArray()
        val iovLen = ByteBuffer.allocate(Integer.BYTES).putInt(string.length).array().reversed().toByteArray()

        return backend.allocator.prealloc(byteArrayOf(*iovPtr, *iovLen), alignment = 4)
    }

}
