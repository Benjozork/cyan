package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<WasmCompilerBackend, WasmLoweringContext, FirFunctionDeclaration> {

    override fun lower(context: WasmLoweringContext, item: FirFunctionDeclaration): String {
        if (item.name == "wasmMain")
            return context.backend.generateStartSymbol(item.block)

        val functionName = item.name
        val functionArguments = item.args.joinToString(" ") { it.typeAnnotation.toString() }

        return """
        |(func ${"$"}$functionName (param $functionArguments)
        |${item.block.statements.joinToString("\n") { context.backend.lowerStatement(it, context).prependIndent("    ") }}
        |)
        """.trimMargin()
    }

}
