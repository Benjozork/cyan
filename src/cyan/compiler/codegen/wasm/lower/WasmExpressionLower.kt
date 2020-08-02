package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionCall

object WasmExpressionLower : FirItemLower<WasmCompilerBackend, FirExpression> {

    override fun lower(backend: WasmCompilerBackend, item: FirExpression): String {
        return when {
            item is FirExpression.Literal &&
            item.parent is FirFunctionCall &&
            (item.parent as FirFunctionCall).callee.resolvedSymbol.name == "print" -> {
                "(i32.const ${backend.allocator.allocateAnyAsStringIov(item)})"
            }
            else -> when (item) {
                is FirExpression.Literal.Number -> {
                    "(i32.const ${item.value})"
                }
                is FirExpression.Literal.String -> {
                    val preAllocationAddress = backend.allocator.allocateStringIov(item.value)

                    "(i32.const $preAllocationAddress)"
                }
                else -> error("fir2wasm: cannot lower expression of type '${item::class.simpleName}'")
            }
        }
    }

}
