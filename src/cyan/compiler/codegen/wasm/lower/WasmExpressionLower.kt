package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.utils.AllocationResult
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.operator.CyanBinaryMinusOperator
import cyan.compiler.parser.ast.operator.CyanBinaryPlusOperator
import cyan.compiler.parser.ast.operator.CyanBinaryTimesOperator

object WasmExpressionLower : FirItemLower<WasmCompilerBackend, WasmLoweringContext, FirExpression> {

    override fun lower(context: WasmLoweringContext, item: FirExpression): String {
//        return if (item.parent is FirFunctionCall && (item.parent as FirFunctionCall).callee.resolvedSymbol.name == "print")
//            when (item) {
//                is FirExpression.Literal.String -> "(i32.const ${context.backend.allocator.allocateStringIov(item.value)})"
//                is FirExpression.Literal.Number -> "(i32.const ${context.backend.allocator.allocateStringIov(item.value.toString())})"
//                else -> error("fir2wasm-print-formatter: cannot format value of type '${item::class.simpleName}'")
//            }
        return when (item) {
            is FirExpression.Literal -> when (val allocationResult = context.backend.allocator.allocate(item)) {
                is AllocationResult.Stack -> "(i32.const ${allocationResult.literal})"
                is AllocationResult.Heap -> "(i32.const ${allocationResult.pointer})"
            }
            is FirExpression.ArrayIndex -> when (val base = item.base) {
                is FirResolvedReference -> {
                    val originalDeclaration = base.resolvedSymbol

                    require (originalDeclaration is FirVariableDeclaration)

                    val ptr = context.pointerForLocal[originalDeclaration] ?: error("fir2wasm: no ptr local generated for '${originalDeclaration.name}'")
                    val idx = (item.index as? FirExpression.Literal.Number)?.value ?: error("fir2wasm: array indexes are currently only supported with numeric literals")

                    when (val declType = originalDeclaration.initializationExpr.type()) {
                        Type.Primitive(CyanType.Str, true),
                        Type.Primitive(CyanType.I32, true) -> "(call \$cy_array_get_i32 (i32.const $ptr) (i32.const $idx))"
                        else -> error("fir2wasm: cannot lower array index on array of type '$declType'")
                    }
                }
                else -> error("fir2wasm: array indexes are currently only supported on references")
            }
            is FirExpression.Binary -> when (item.commonType) {
                null -> error("fir2wasm: cannot lower binary expression with different operands")
                Type.Primitive(CyanType.I32, false) -> when (item.operator) {
                    CyanBinaryPlusOperator   -> "(i32.add ${lower(context, item.lhs.realExpr)} ${lower(context, item.rhs.realExpr)})"
                    CyanBinaryMinusOperator  -> "(i32.sub ${lower(context, item.lhs.realExpr)} ${lower(context, item.rhs.realExpr)})"
                    CyanBinaryTimesOperator  -> "(i32.mul ${lower(context, item.lhs.realExpr)} ${lower(context, item.rhs.realExpr)})"
                    else -> error("fir2wasm: cannot lower binary expression with operator '${item.operator::class.simpleName}'")
                }
                else -> error("fir2wasm: cannot lower binary expression operand type ''${item.lhs.realExpr.type()}")
            }
            else -> error("fir2wasm: cannot lower expression of type '${item::class.simpleName}'")
        }

    }

}
