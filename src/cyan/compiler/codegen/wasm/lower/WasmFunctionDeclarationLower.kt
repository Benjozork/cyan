package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.dsl.Wasm.Type.*
import cyan.compiler.codegen.wasm.dsl.WasmFunction
import cyan.compiler.codegen.wasm.dsl.func
import cyan.compiler.codegen.wasm.dsl.local
import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<WasmLoweringContext, FirFunctionDeclaration, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirFunctionDeclaration): Wasm.OrderedElement {
        val wasmInstructionsAttribute = item.attributes.find { it.ident.text == "wasm_instructions" }

        if (wasmInstructionsAttribute != null) {
            if (wasmInstructionsAttribute !is FirFunctionDeclaration.Attribute.Value || !(Type.Primitive(CyanType.Str, true) accepts wasmInstructionsAttribute.expr.type())) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        astNode = item.fromAstNode,
                        message = "wasm_instructions attribute must be assigned to an array of strings",
                        span = wasmInstructionsAttribute.fromAstNode.span
                    )
                )
            }

            return WasmInlineInstructionsFunctionLower.lower(context, item)
        }

        val functionName = item.name
        val isStartExport = item.name == "_start"

        val wasmFunctionParameters = (item.args.toList() + item.receiver?.let { FirFunctionArgument(item, "_r", it.type) })
                .filterNotNull().map { arg -> WasmFunction.Parameter(arg.name, i32) }.toTypedArray()

        val wasmReturnType = when (item.returnType) {
            Type.Primitive(CyanType.I32),
            Type.Primitive(CyanType.Bool),
            Type.Primitive(CyanType.Str),
            is Type.Struct -> i32
            Type.Primitive(CyanType.Void) -> null
            else -> error("fir2wasm: cyan return type '${item.returnType}' not supported yet")
        }

        return func(functionName, *wasmFunctionParameters, returnType = wasmReturnType, exportedAs = if (isStartExport) "_start" else null) {
            if (isStartExport)
                call("cy_init_heap")

            for (statement in item.block.statements) {
                +context.backend.lowerStatement(statement, context)
            }

            for (localNum in context.locals.values) {
                local.new(localNum, i32)
            }
        }
    }

}
