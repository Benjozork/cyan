package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirDocument
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

import java.lang.StringBuilder

object JsExpressionLower : FirItemLower<JsCompilerBackend, FirExpression> {

    override fun lower(backend: JsCompilerBackend, item: FirExpression): String {
        return when (val expr = item.astExpr) {
            is CyanIdentifierExpression -> expr.value
            is CyanNumericLiteralExpression -> "${expr.value}"
            is CyanStringLiteralExpression  -> "'${expr.value.replace("'", "\\'")}'"
            is CyanBooleanLiteralExpression -> "${expr.value}"
            is CyanFunctionCall -> {
                val containingDocument = item.firstAncestorOfType<FirDocument>()
                    ?: error("fir2js: no FirDocument as ancestor of node")

                val calleeName = expr.functionIdentifier.value

                val isBuiltin = containingDocument.localFunctions.any { it.isExtern && it.name == calleeName }

                val jsName = if (isBuiltin) "builtins.$calleeName" else calleeName

                "$jsName(${expr.args.joinToString(", ") { backend.lowerExpression(FirExpression(item, it)) }})"
            }
            is CyanStructLiteralExpression -> {
                val structType = item.type() as Type.Struct

                val builder = StringBuilder()

                builder.append("{ ")
                for ((index, fieldName) in structType.properties.withIndex()) {
                    builder.append("${fieldName.name}: ${expr.exprs[index]}")
                    if (index < structType.properties.size - 1) builder.append(", ")
                }
                builder.append(" }")

                builder.toString()
             }
            is CyanBinaryExpression -> {
                val lhs = backend.lowerExpression(FirExpression(item, expr.lhs))
                val rhs = backend.lowerExpression(FirExpression(item, expr.rhs))

                "$lhs ${expr.operator} $rhs"
            }
            is CyanArrayExpression -> expr.exprs.joinToString(prefix = "[", postfix = "]", separator = ", ") { backend.lowerExpression(FirExpression(item, it)) }
            is CyanMemberAccessExpression -> {
                val loweredBase = backend.lowerExpression(FirExpression(item, expr.base))
                val member = backend.lowerExpression(FirExpression(item, expr.member))

                "$loweredBase.$member"
            }
            is CyanArrayIndexExpression -> "${backend.lowerExpression(FirExpression(item, expr.base))}[${expr.index}]"
            else -> error("fir2js: cannot lower expression of type '${item.astExpr::class.simpleName}'")
        }
    }

}
