package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.functions.FirFunctionCall

object WasmStatementLower : FirItemLower<WasmCompilerBackend, WasmLoweringContext, FirStatement> {

    override fun lower(context: WasmLoweringContext, item: FirStatement): String {
        return when (item) {
            is FirVariableDeclaration -> {
                val ptr = when (val allocationResult = context.backend.allocator.allocate(item.initializationExpr)) {
                    is AllocationResult.Stack -> allocationResult.literal
                    is AllocationResult.Heap -> allocationResult.pointer
                }

                context.pointerForLocal[item] = ptr
                context.numLocals++

                // "(local.set ${context.numLocals} (i32.const $ptr))"
                ""
            }
            is FirFunctionCall -> {
                val name = item.callee.resolvedSymbol.name
                val exprs = item.args.joinToString(" ") { expr -> context.backend.lowerExpression(expr, context) }

                "(call \$$name ${exprs})\ndrop"
            }
            else -> error("fir2wasm: couldn't lower statement of type '${item::class.simpleName}'")
        }
    }

}
