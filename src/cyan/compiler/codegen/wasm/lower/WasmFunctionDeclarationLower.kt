package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.dsl.WasmFunction.Parameter.Companion.param
import cyan.compiler.codegen.wasm.dsl.func
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmFunctionDeclarationLower : FirItemLower<WasmLoweringContext, FirFunctionDeclaration, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirFunctionDeclaration): Wasm.OrderedElement {
        val functionName = item.name
        val isStartExport = item.name == "_start"

        return func(functionName, *item.args.map { param(it.name, "i32")}.toTypedArray(), exportedAs = if (isStartExport) "_start" else null) {
            for (statement in item.block.statements) {
                +context.backend.lowerStatement(statement, context)
            }

            for (local in context.locals.values) {
                +Wasm.Instruction("(local \$$local i32)")
            }
        }
    }

}
