package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsLoweringContext
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.firstAncestorOfType

import java.lang.StringBuilder

object JsStatementLower : FirItemLower<JsLoweringContext, FirStatement, String> {

    override fun lower(context: JsLoweringContext, item: FirStatement): String {
        return when (item) {
            is FirVariableDeclaration -> {
                "${if (!item.mutable) "const" else "let"} ${item.name} = ${context.backend.lowerExpression(item.initializationExpr, context)};"
            }
            is FirExpression -> JsExpressionLower.lower(context, item)
            is FirIfChain -> {
                val builder = StringBuilder()

                for ((index, branch) in item.branches.withIndex()) {
                    if (index > 0) builder.append(" else ")
                    builder.append("if (${context.backend.lowerExpression(branch.first, context)}) {\n")
                    builder.append(context.backend.translateSource(branch.second, context).prependIndent("    "))
                    builder.append("\n}")
                }

                if (item.elseBranch != null) {
                    builder.append(" else {\n")
                    builder.append(context.backend.translateSource(item.elseBranch!!, context).prependIndent("    "))
                    builder.append("\n}")
                }

                builder.toString()
            }
            is FirAssignment -> {
                "${item.targetVariable!!.name} = ${context.backend.lowerExpression(item.newExpr!!, context)};"
            }
            is FirReturn -> {
                "return ${context.backend.lowerExpression(item.expr, context)};"
            }
            else -> error("fir2js: cannot lower statement of type '${item::class.simpleName}'")
        }
    }

}
