package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.FirItemLower
import cyan.compiler.fir.FirStatement

object WasmStatementLower : FirItemLower<FirStatement> {

    override fun lower(backend: FirCompilerBackend, item: FirStatement): String {
        TODO()
    }

}
