package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.*
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression

object WasmStatementLower : FirItemLower<WasmLoweringContext, FirStatement, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirStatement): Wasm.OrderedElement {
        return when (item) {
            is FirVariableDeclaration -> when (val expr = item.initializationExpr) {
                is FirExpression.Literal -> {
                    if (expr.isConstant) {
                        val allocationResult = context.allocator.preAllocate(item.initializationExpr)

                        val localId = context.addLocal(item)

                        instructions {
                            i32.const(when (allocationResult) {
                                is AllocationResult.Heap  -> allocationResult.pointer
                                is AllocationResult.Stack -> allocationResult.literal
                            })
                            local.set(localId)
                        }
                    } else instructions {
                        val localId = context.addLocal(item)

                        +context.backend.lowerExpression(expr, context)

                        local.set(localId)
                    }
                }
                else -> {
                    val localId = context.addLocal(item)

                    instructions {
                        +context.backend.lowerExpression(item.initializationExpr, context)
                        local.set(localId)
                    }
                }
            }
            is FirExpression -> context.backend.lowerExpression(item, context)
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
                    item.block!!.statements.map { +context.backend.lowerStatement(it, context) }
                }

                +loweredExpression
                i32.eqz
                br_if(0)
                loop(0) {
                    +loweredBody
                    +loweredExpression
                    br_if(0)
                }
            }
            is FirAssignment -> {
                val symbol = item.targetVariable!!
                val loweredNewExpr = context.backend.lowerExpression(item.newExpr!!, context)

                instructions {
                    +loweredNewExpr
                    local.set(context.locals[symbol] ?: error("no local was set for symbol '${symbol.name}'"))
                }
            }
            is FirReturn -> context.backend.lowerExpression(item.expr, context)
            else -> error("fir2wasm: couldn't lower statement of type '${item::class.simpleName}'")
        }
    }

}
