package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirModule
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.firstAncestorOfType

import java.lang.StringBuilder

object JsExpressionLower : FirItemLower<JsCompilerBackend, FirExpression> {

    override fun lower(backend: JsCompilerBackend, item: FirExpression): String {
        return when (val expr = item.realExpr) {
            is FirResolvedReference -> expr.text
            is FirExpression.Literal.Number -> "${expr.value}"
            is FirExpression.Literal.String  -> "'${expr.value.replace("'", "\\'")}'"
            is FirExpression.Literal.Boolean -> "${expr.value}"
            is FirExpression.FunctionCall -> {
                val containingDocument = item.firstAncestorOfType<FirModule>()
                    ?: error("fir2js: no FirDocument as ancestor of node")

                val calleeName = expr.callee.resolvedSymbol.name

                val isBuiltin = containingDocument.localFunctions.any { it.isExtern && it.name == calleeName }

                val jsName = if (isBuiltin) "builtins.$calleeName" else calleeName

                "$jsName(${expr.args.joinToString(", ", transform = backend::lowerExpression)})"
            }
            is FirExpression.Literal.Struct -> {
                val structType = item.type() as Type.Struct

                val builder = StringBuilder()

                builder.append("{ ")
                for ((index, field) in structType.properties.withIndex()) {
                    builder.append("${field.name}: ${expr.elements.entries.first { it.key.name == field.name }.value.let { backend.lowerExpression(it) }}")
                    if (index < structType.properties.size - 1) builder.append(", ")
                }
                builder.append(" }")

                builder.toString()
             }
            is FirExpression.Binary -> {
                val lhs = backend.lowerExpression(expr.lhs)
                val rhs = backend.lowerExpression(expr.rhs)

                "$lhs ${expr.operator} $rhs"
            }
            is FirExpression.Literal.Array -> expr.elements.joinToString(prefix = "[", postfix = "]", separator = ", ") { backend.lowerExpression(it) }
            is FirExpression.MemberAccess -> {
                val loweredBase = backend.lowerExpression(expr.base)

                "$loweredBase.${expr.member}"
            }
            is FirExpression.ArrayIndex -> "${backend.lowerExpression(expr.base)}[${backend.lowerExpression(expr.index)}]"
            else -> error("fir2js: cannot lower expression of type '${this::class.simpleName}'")
        }
    }

}
