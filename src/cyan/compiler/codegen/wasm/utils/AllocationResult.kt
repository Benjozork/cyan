package cyan.compiler.codegen.wasm.utils

sealed class AllocationResult {

    class Heap(val pointer: Int) : AllocationResult()

    class Stack(val literal: Int) : AllocationResult()

}
