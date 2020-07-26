package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.FirItemLower
import cyan.compiler.fir.expression.FirExpression

object WasmExpressionLower : FirItemLower<FirExpression> {

    override fun lower(backend: FirCompilerBackend, item: FirExpression): String {
        TODO()
    }

}
