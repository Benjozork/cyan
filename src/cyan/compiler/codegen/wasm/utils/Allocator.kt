package cyan.compiler.codegen.wasm.utils

class Allocator {

    val heap = ByteArray(64)

    var heapEndPtr = 0x00

    fun prealloc(bytes: ByteArray, alignment: Byte? = null): Int {
        val baseAddr = heapEndPtr.let {
            if (alignment != null) {
                ((it + alignment - 1) / alignment) * alignment
            } else it
        }

        if (heap.size - bytes.size < baseAddr)
            error("no more space to allocate")

        var addr = baseAddr
        for (byte in bytes) {
            heap[addr] = byte
            addr++
        }

        heapEndPtr = addr
        return baseAddr
    }

}
