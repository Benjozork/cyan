package cyan.compiler.codegen.wasm.utils

import cyan.compiler.fir.expression.FirExpression

import java.nio.ByteBuffer

class Allocator {

    private fun arrayToBytes(array: FirExpression.Literal.Array): List<Byte> {
        val bytes = mutableListOf<Byte>()

        for (element in array.elements) {
            bytes += toBytes(element)
        }

        return bytes
    }

    private fun toBytes(expression: FirExpression): List<Byte> = when (expression) {
        is FirExpression.Literal.Scalar<*> -> when (val value = expression.value) {
            is String -> value.toByteArray().toList()
            is Int -> {
                val buffer = ByteBuffer.allocate(Integer.BYTES).putInt(value).array().reversed().toByteArray()

                buffer.toList()
            }
            else -> error("fir2wasm-value-transformer: cannot transform scalar value of type '${expression::class.simpleName}'")
        }
        is FirExpression.Literal.Array -> arrayToBytes(expression)
        else -> error("fir2wasm-value-transformer: cannot transform value of type '${expression::class.simpleName}'")
    }

    fun allocate(expression: FirExpression): AllocationResult = when (expression) {
        is FirExpression.Literal.String -> AllocationResult.Heap(prealloc(toBytes(expression).toByteArray(), alignment = 4))
        is FirExpression.Literal.Scalar<*> -> AllocationResult.Stack (
            when(val value = expression.value) {
                is Int -> value
                else -> error("fir2wasm-allocator: cannot allocate scalar value of type '${expression::class.simpleName}'")
            }
        )
        is FirExpression.Literal -> AllocationResult.Heap(prealloc(toBytes(expression).toByteArray()))
        else -> error("fir2wasm-allocator: cannot allocate value of type '${expression::class.simpleName}'")
    }

    fun allocateStringIov(string: String): Int {
        val bytes = string.toByteArray()

        val pointerTarget = prealloc(bytes)

        val iovPtr = ByteBuffer.allocate(Integer.BYTES).putInt(pointerTarget).array().reversed().toByteArray()
        val iovLen = ByteBuffer.allocate(Integer.BYTES).putInt(string.length).array().reversed().toByteArray()

        return prealloc(byteArrayOf(*iovPtr, *iovLen), alignment = 4)
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
