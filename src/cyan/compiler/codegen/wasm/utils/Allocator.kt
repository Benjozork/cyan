package cyan.compiler.codegen.wasm.utils

import cyan.compiler.fir.expression.FirExpression

import java.nio.ByteBuffer

class Allocator {

    private fun Int.bytes() =  ByteBuffer.allocate(Integer.BYTES).putInt(this).array().reversed().toByteArray()

    private fun arrayToBytes(array: FirExpression.Literal.Array): List<Byte> {
        val arrLen = array.elements.size.bytes()

        val bytes = mutableListOf(*arrLen.toTypedArray())

        for (element in array.elements) {
            bytes += when (element) {
                is FirExpression.Literal.String -> allocateStringNullTerminated(element.value).bytes().toMutableList()
                is FirExpression.Literal.Scalar<*> -> toBytes(element)
                else -> error("fir2wasm-allocator: cannot statically allocate array of type '${array.type()}'")
            }
        }

        return bytes
    }

    private fun structToBytes(expression: FirExpression.Literal.Struct): List<Byte> {
        val bytes = mutableListOf<Byte>()

        for (field in expression.elements.values) {
            bytes += when (val allocation = preAllocate(field)) {
                is AllocationResult.Heap  -> allocation.pointer.bytes().toList()
                is AllocationResult.Stack -> allocation.literal.bytes().toList()
            }
        }

        return bytes
    }

    private fun toBytes(expression: FirExpression): List<Byte> = when (expression) {
        is FirExpression.Literal.Scalar<*> -> when (val value = expression.value) {
            is String -> value.toByteArray().toList() + 0x00
            is Int -> {
                val buffer = ByteBuffer.allocate(Integer.BYTES).putInt(value).array().reversed().toByteArray()

                buffer.toList()
            }
            else -> error("fir2wasm-value-transformer: cannot transform scalar value of type '${expression::class.simpleName}'")
        }
        is FirExpression.Literal.Array  -> arrayToBytes(expression)
        is FirExpression.Literal.Struct -> structToBytes(expression)
        else -> error("fir2wasm-value-transformer: cannot transform value of type '${expression::class.simpleName}'")
    }

    fun preAllocate(expression: FirExpression): AllocationResult = when (expression) {
        is FirExpression.Literal.String -> AllocationResult.Heap(prealloc(toBytes(expression).toByteArray(), alignment = 4))
        is FirExpression.Literal.Scalar<*> -> AllocationResult.Stack (
            when(val value = expression.value) {
                is Int -> value
                is Boolean -> if (value) 1 else 0
                else -> error("fir2wasm-allocator: cannot allocate scalar value of type '${expression::class.simpleName}'")
            }
        )
        is FirExpression.Literal.Struct -> AllocationResult.Heap(prealloc(toBytes(expression).toByteArray(), alignment = 4))
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

    fun allocateStringNullTerminated(string: String): Int {
        return prealloc(string.toByteArray() + 0x00)
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
