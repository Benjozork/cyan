package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.FirItemLower
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<FirFunctionDeclaration> {

    override fun lower(backend: FirCompilerBackend, item: FirFunctionDeclaration): String {
        TODO()
    }

}
