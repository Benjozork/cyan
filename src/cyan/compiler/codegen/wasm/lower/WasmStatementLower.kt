package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.FirStatement

object WasmStatementLower : FirItemLower<WasmCompilerBackend, FirStatement> {

    override fun lower(backend: WasmCompilerBackend, item: FirStatement): String {
        TODO()
    }

}
