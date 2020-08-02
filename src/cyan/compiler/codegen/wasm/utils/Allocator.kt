package cyan.compiler.codegen.wasm.utils

import cyan.compiler.fir.expression.FirExpression
import java.nio.ByteBuffer

class Allocator {

    fun allocateStringIov(string: String): Int {
        val bytes = string.toByteArray()

        val pointerTarget = prealloc(bytes)

        val iovPtr = ByteBuffer.allocate(Integer.BYTES).putInt(pointerTarget).array().reversed().toByteArray()
        val iovLen = ByteBuffer.allocate(Integer.BYTES).putInt(string.length).array().reversed().toByteArray()

        return prealloc(byteArrayOf(*iovPtr, *iovLen), alignment = 4)
    }

    fun allocateAnyAsStringIov(item: FirExpression.Literal): Int {
        return when (item) {
            is FirExpression.Literal.Scalar<*> -> allocateStringIov(item.value.toString())
            is FirExpression.Literal.Array -> allocateStringIov(item.elements.joinToString(", ", "[", "]") {
                (it as? FirExpression.Literal.Scalar<*>)?.value?.toString() ?: error("fir2wasm-value-transformer: array element was not scalar")
            })
            else -> error("fir2wasm-value-transformer: cannot transform value of type '${item::class.simpleName}' to a string")
        }
    }

    var heap = ByteArray(64)

    var heapEndPtr = 0x00

    private fun prealloc(bytes: ByteArray, alignment: Byte? = null): Int {
        val baseAddr = heapEndPtr.let {
            if (alignment != null) {
                ((it + alignment - 1) / alignment) * alignment
            } else it
        }

        while (heap.size - bytes.size < baseAddr)
            growHeap()

        var addr = baseAddr
        for (byte in bytes) {
            heap[addr] = byte
            addr++
        }

        heapEndPtr = addr
        return baseAddr
    }

    private fun growHeap() {
        val newHeap = ByteArray(heap.size * 2)
        System.arraycopy(heap, 0, newHeap, 0, heap.size)
        heap = newHeap
    }

}
