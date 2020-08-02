package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<WasmCompilerBackend, FirFunctionDeclaration> {

    override fun lower(backend: WasmCompilerBackend, item: FirFunctionDeclaration): String {
        if (item.name == "wasmMain")
            return backend.generateStartSymbol(item.block)

        val functionName = item.name
        val functionArguments = item.args.joinToString(" ") { it.typeAnnotation.toString() }

        return """
        (func ${"$"}$functionName (param $functionArguments)
            ${item.block.statements.joinToString("\n", transform = backend::lowerStatement)}
        )
        """.trimIndent()
    }

}
