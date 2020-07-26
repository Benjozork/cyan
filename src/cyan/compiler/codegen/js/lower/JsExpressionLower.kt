package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.FirItemLower
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanArrayIndexExpression
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

object JsExpressionLower : FirItemLower<FirExpression> {

    override fun lower(backend: FirCompilerBackend, item: FirExpression): String {
        return when (val expr = item.astExpr) {
            is CyanIdentifierExpression -> expr.value
            is CyanNumericLiteralExpression -> "${expr.value}"
            is CyanStringLiteralExpression  -> "'${expr.value.replace("'", "\\'")}'"
            is CyanBooleanLiteralExpression -> "${expr.value}"
            is CyanBinaryExpression -> {
                val lhs = backend.lowerExpression(FirExpression(item, expr.lhs))
                val rhs = backend.lowerExpression(FirExpression(item, expr.rhs))

                "$lhs ${expr.operator} $rhs"
            }
            is CyanArrayExpression -> expr.exprs.joinToString(prefix = "[", postfix = "]", separator = ", ") { backend.lowerExpression(FirExpression(item, it)) }
            is CyanArrayIndexExpression -> "${backend.lowerExpression(FirExpression(item, expr.base))}[${expr.index}]"
            else -> error("fir2js: cannot lower expression of type '${item.astExpr::class.simpleName}'")
        }
    }

}
