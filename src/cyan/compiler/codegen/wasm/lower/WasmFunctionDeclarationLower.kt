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
        val functionExport = if (item.name == "main") " (export \"_start\") " else " "
        val functionArguments = item.args.joinToString(" ") { it.typeAnnotation.toString() }

        val statements = item.block.statements.joinToString("\n") { context.backend.lowerStatement(it, context).prependIndent("    ") }

        val locals = context.locals.values.joinToString("\n", postfix = "\n") { "(local $$it i32)".prependIndent("    ") }

        return """
        |(func ${"$"}$functionName$functionExport${functionArguments.takeIf { it.isNotBlank() } ?: ""}
        |$locals
        |$statements
        |)
        """.trimMargin()
    }

}
