package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.dsl.i32
import cyan.compiler.codegen.wasm.dsl.instructions
import cyan.compiler.codegen.wasm.dsl.local
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.codegen.wasm.utils.ValueSerializer
import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.fir.functions.FirFunctionCall
import cyan.compiler.parser.ast.operator.*

object WasmExpressionLower : FirItemLower<WasmLoweringContext, FirExpression, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirExpression): Wasm.OrderedElement {
        return if (item.parent is FirFunctionCall && (item.parent as FirFunctionCall).callee.resolvedSymbol.name == "print") instructions {
            i32.const(ValueSerializer.convert(item).let { context.allocator.allocateStringIov(it) })
        } else when (val expr = item.realExpr) {
            is FirExpression.Literal -> instructions {
                when (val allocationResult = context.allocator.allocate(expr)) {
                    is AllocationResult.Stack -> i32.const(allocationResult.literal)
                    is AllocationResult.Heap  -> i32.const(allocationResult.pointer)
                }
            }
            is FirExpression.MemberAccess -> {
                val baseStruct = expr.base.type() as Type.Struct
                val baseStructField = baseStruct.properties.first { it.name == expr.member }

                val baseValuePtr = when (expr.base) {
                    is FirResolvedReference -> when (val symbol = expr.base.resolvedSymbol) {
                        is FirVariableDeclaration -> context.pointerForLocal[symbol] ?: error("no local created for symbol '${symbol.name}'")
                        else -> error("fir2wasm: cannot make structure base ptr for ${symbol::class.simpleName}")
                    }
                    else -> error("fir2wasm: member access is currently only supported on references")
                }

                val fieldIndex = baseStruct.properties.indexOfFirst { it == baseStructField }.takeIf { it > 0 }
                        ?: error("fir2wasm: base struct field index was -1")

                return instructions {
                    i32.const(baseValuePtr + (fieldIndex * 4))
                }
            }
            is FirExpression.ArrayIndex -> when (val base = expr.base) {
                is FirResolvedReference -> {
                    val originalDeclaration = base.resolvedSymbol

                    require(originalDeclaration is FirVariableDeclaration)

                    val ptr = context.pointerForLocal[originalDeclaration]
                        ?: error("fir2wasm: no ptr local generated for '${originalDeclaration.name}'")
                    val idx = (expr.index as? FirExpression.Literal.Number)?.value
                        ?: error("fir2wasm: array indexes are currently only supported with numeric literals")

                    when (val declType = originalDeclaration.initializationExpr.type()) {
                        Type.Primitive(CyanType.Str, true),
                        Type.Primitive(CyanType.I32, true) -> Wasm.Instruction("(call \$cy_array_get_i32 (i32.const $ptr) (i32.const $idx))")
                        else -> error("fir2wasm: cannot lower array index on array of type '$declType'")
                    }
                }
                else -> error("fir2wasm: array indexes are currently only supported on references")
            }
            is FirExpression.Binary -> when (expr.commonType) {
                null -> error("fir2wasm: cannot lower binary expression with different operands")
                Type.Primitive(CyanType.I32, false) -> instructions {
                    +lower(context, expr.rhs)
                    +lower(context, expr.lhs)

                    when (expr.operator) {
                        CyanBinaryPlusOperator          -> i32.add
                        CyanBinaryMinusOperator         -> i32.sub
                        CyanBinaryTimesOperator         -> i32.mul
                        CyanBinaryLesserOperator        -> i32.lt_u
                        CyanBinaryLesserEqualsOperator  -> i32.le_u
                        CyanBinaryGreaterOperator       -> i32.gt_u
                        CyanBinaryGreaterEqualsOperator -> i32.ge_u
                        else -> error("fir2wasm: cannot lower binary expression with operator '${expr.operator::class.simpleName}'")
                    }
                }
                else -> error("fir2wasm: cannot lower binary expression operand type ''${expr.lhs.realExpr.type()}")
            }
            is FirResolvedReference -> {
                val symbol = expr.resolvedSymbol

                val containingScope = item.containingScope()

                val localVariable = containingScope?.declaredSymbols?.firstOrNull { it == symbol } as? FirVariableDeclaration ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "wasm target currently only supports references to local variables or function arguments",
                        astNode = item.fromAstNode
                    )
                )

                instructions {
                    local.get(context.locals[localVariable] ?: error("no local created for ${localVariable.name}"))
                }
            }
            else -> error("fir2wasm: cannot lower expression of type '${expr::class.simpleName}'")
        }

    }

}
