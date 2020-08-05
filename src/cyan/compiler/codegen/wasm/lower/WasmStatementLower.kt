package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.*
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionCall
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmStatementLower : FirItemLower<WasmLoweringContext, FirStatement, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirStatement): Wasm.OrderedElement {
        return when (item) {
            is FirVariableDeclaration -> {
                val value = when (val allocationResult = context.backend.allocator.allocate(item.initializationExpr)) {
                    is AllocationResult.Stack -> allocationResult.literal
                    is AllocationResult.Heap -> allocationResult.pointer
                }

                context.pointerForLocal[item] = value
                val localId = context.addLocal(item)

                instructions {
                    local.set(localId, value)
                }
            }
            is FirFunctionCall -> {
                val function = item.callee.resolvedSymbol as FirFunctionDeclaration
                val functionReturnTypeIsVoid = function.returnType == Type.Primitive(CyanType.Void, false)

                instructions {
                    for (argument in item.args.map { context.backend.lowerExpression(it, context) }) {
                        +argument
                    }

                    call(function.name)
                    if (!functionReturnTypeIsVoid)
                        drop
                }
            }
            is FirIfChain -> {
                require (item.branches.size == 1) { "fir2wasm: if chains can currently only have one condition" }
                require (item.elseBranch != null) { "fir2wasm: if chains currently must have an else branch" }

                val loweredExpression = instructions {
                    +context.backend.lowerExpression(item.branches.first().first, context)
                }

                condition(0, loweredExpression, {
                    item.branches.first().second.statements.forEach { this.ifElements += context.backend.lowerStatement(it, context) }
                }, otherwise = {
                    item.elseBranch!!.statements.forEach { this.elseElements += context.backend.lowerStatement(it, context) }
                })
            }
            is FirWhileStatement -> block(0) {
                val loweredExpression = context.backend.lowerExpression(item.conditionExpr, context)
                val loweredBody = instructions {
                    item.block.statements.map { +context.backend.lowerStatement(it, context) }
                }

                +loweredExpression
                br_if(0)
                loop(0) {
                    +loweredBody
                    +loweredExpression
                    i32.eqz
                    br_if(0)
                }
            }
            is FirAssignment -> {
                val symbol = item.targetVariable
                val loweredNewExpr = context.backend.lowerExpression(item.newExpr!!, context)

                Wasm.Instruction("(local.set \$${context.locals[symbol]} $loweredNewExpr)")
            }
            else -> error("fir2wasm: couldn't lower statement of type '${item::class.simpleName}'")
        }
    }

}
