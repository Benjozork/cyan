package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.expression.FirExpression

object WasmExpressionLower : FirItemLower<WasmCompilerBackend, FirExpression> {

    override fun lower(backend: WasmCompilerBackend, item: FirExpression): String {
        return when (item) {
            is FirExpression.Literal.Number -> "(i32.const ${item.value})"
            is FirExpression.Literal.String -> {
                val preAllocationAddress = WasmStringTransformer.stringToIov(backend, item.value)

                "(i32.const $preAllocationAddress)"
            }
            else -> error("fir2wasm: cannot lower expression of type '${item::class.simpleName}'")
        }
    }

}
