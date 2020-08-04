package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.functions.FirFunctionCall
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.parser.ast.FirWhileStatement

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

                "(call \$$name ${exprs})" +
                        if ((item.callee.resolvedSymbol as FirFunctionDeclaration).returnType != Type.Primitive(CyanType.Void, false)) "\ndrop" else ""
            }
            is FirWhileStatement -> {
                """
                |(block ${"$"}B0
                |${context.backend.lowerExpression(item.conditionExpr, context).prependIndent("    ")}
                |    br_if ${"$"}B0
                |    (loop ${"$"}L0
                |${item.block.statements.joinToString("\n") { context.backend.lowerStatement(it, context).prependIndent("        ") }}
                |        br ${"$"}L0
                |    )
                |)
            """.trimMargin()
            }
            else -> error("fir2wasm: couldn't lower statement of type '${item::class.simpleName}'")
        }
    }

}
