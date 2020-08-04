package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.dsl.i32
import cyan.compiler.codegen.wasm.dsl.instructions
import cyan.compiler.codegen.wasm.dsl.local
import cyan.compiler.codegen.wasm.utils.AllocationResult
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
        return if (item.parent is FirFunctionCall && (item.parent as FirFunctionCall).callee.resolvedSymbol.name == "print")
            when (item) {
                is FirExpression.Literal.String -> Wasm.Instruction("(i32.const ${context.backend.allocator.allocateStringIov(item.value)})")
                is FirExpression.Literal.Number -> Wasm.Instruction("(i32.const ${context.backend.allocator.allocateStringIov(item.value.toString())})")
                else -> error("fir2wasm-print-formatter: cannot format value of type '${item::class.simpleName}'")
            }
        else when (val expr = item.realExpr) {
            is FirExpression.Literal -> instructions {
                when (val allocationResult = context.backend.allocator.allocate(expr)) {
                    is AllocationResult.Stack -> i32.const(allocationResult.literal)
                    is AllocationResult.Heap  -> i32.const(allocationResult.pointer)
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
                        CyanBinaryPlusOperator   -> i32.add
                        CyanBinaryMinusOperator  -> i32.sub
                        CyanBinaryTimesOperator  -> i32.mul
                        CyanBinaryLesserOperator -> i32.lt_u
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
