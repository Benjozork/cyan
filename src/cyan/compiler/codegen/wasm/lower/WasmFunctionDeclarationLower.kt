package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<WasmCompilerBackend, FirFunctionDeclaration> {

    override fun lower(backend: WasmCompilerBackend, item: FirFunctionDeclaration): String {
        TODO()
    }

}
