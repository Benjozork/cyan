package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionCall

object WasmExpressionLower : FirItemLower<WasmCompilerBackend, FirExpression> {

    override fun lower(backend: WasmCompilerBackend, item: FirExpression): String {
        return if (item.parent is FirFunctionCall && (item.parent as FirFunctionCall).callee.resolvedSymbol.name == "print")
            when (item) {
                is FirExpression.Literal.String -> "(i32.const ${backend.allocator.allocateStringIov(item.value)})"
                is FirExpression.Literal.Number -> "(i32.const ${backend.allocator.allocateStringIov(item.value.toString())})"
                else -> error("fir2wasm-print-formatter: cannot format value of type '${item::class.simpleName}'")
            }
        else when (val allocationResult = backend.allocator.allocate(item)) {
            is AllocationResult.Stack -> "(i32.const ${allocationResult.literal})"
            is AllocationResult.Heap -> "(i32.const ${allocationResult.pointer})"
        }
    }

}
