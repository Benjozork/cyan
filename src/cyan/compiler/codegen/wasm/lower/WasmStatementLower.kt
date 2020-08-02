package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.functions.FirFunctionCall

object WasmStatementLower : FirItemLower<WasmCompilerBackend, FirStatement> {
    override fun lower(backend: WasmCompilerBackend, item: FirStatement): String {
        return when (item) {
            is FirFunctionCall -> "(call \$${item.callee.resolvedSymbol.name} ${item.args.joinToString(" ", transform = backend::lowerExpression)})"
            else -> error("fir2wasm: couldn't lower statement of type '${item::class.simpleName}'")
        }
    }

}
