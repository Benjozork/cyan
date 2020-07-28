package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.fir.*
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.fir.functions.FirFunctionCall

import java.lang.StringBuilder

object JsStatementLower : FirItemLower<JsCompilerBackend, FirStatement> {

    override fun lower(backend: JsCompilerBackend, item: FirStatement): String {
        return when (item) {
            is FirFunctionCall -> {
                val isBuiltin = item.firstAncestorOfType<FirDocument>()?.declaredSymbols?.contains(item.callee)
                    ?: error("fir2js: no FirDocument as ancestor of node")

                val jsName = if (isBuiltin) "builtins.${item.callee.name}" else item.callee.name

                "$jsName(${item.args.joinToString(", ", transform = backend::lowerExpression)});"
            }
            is FirAssignment -> {
                "${item.targetVariable!!.name} = ${backend.lowerExpression(item.newExpr!!)};"
            }
            is FirVariableDeclaration -> {
                "${if (!item.mutable) "const" else "let"} ${item.name} = ${backend.lowerExpression(item.initializationExpr)};"
            }
            is FirIfChain -> {
                val builder = StringBuilder()

                for ((index, branch) in item.branches.withIndex()) {
                    if (index > 0) builder.append(" else ")
                    builder.append("if (${backend.lowerExpression(branch.first)}) {\n")
                    builder.append(backend.translateSource(branch.second).prependIndent("    "))
                    builder.append("\n}")
                }

                if (item.elseBranch != null) {
                    builder.append(" else {\n")
                    builder.append(backend.translateSource(item.elseBranch).prependIndent("    "))
                    builder.append("\n}")
                }

                builder.toString()
            }
            else -> error("fir2js: cannot lower statement of type '${item::class.simpleName}'")
        }
    }

}
