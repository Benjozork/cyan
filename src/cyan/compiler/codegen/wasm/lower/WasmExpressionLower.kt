package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

object WasmExpressionLower : FirItemLower<WasmCompilerBackend, FirExpression> {

    override fun lower(backend: WasmCompilerBackend, item: FirExpression): String {
        return when (item.astExpr) {
            is CyanNumericLiteralExpression -> {
                "(i32.const ${item.astExpr.value})"
            }
            is CyanStringLiteralExpression -> {
               "(i32.const ${backend.alloc(item.astExpr.value.toByteArray())})"
            }
            else -> error("fir2wasm: cannot lower expression of type '${item::class.simpleName}'")
        }
    }

}
