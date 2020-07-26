package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.ItemLower
import cyan.compiler.parser.ast.CyanIfChain
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.CyanVariableDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object JsStatementLower : ItemLower<CyanStatement> {

    override fun lower(backend: CompilerBackend, item: CyanStatement): String {
        return when (item) {
            is CyanVariableDeclaration -> "const ${item.name} = ${backend.lowerExpression(item.value)};"
            is CyanFunctionDeclaration -> {
                """
                |function ${item.signature.name}(${item.signature.args.joinToString { it.name }}) {
                |${backend.translateSource(item.source).prependIndent("    ")}
                |}
                """.trimMargin()
            }
            is CyanIfChain -> {
                val blockLowerings = item.ifStatements.mapIndexed { i, branch ->
                    """
                    |${if (i > 0) " else " else ""}if (${backend.lowerExpression(branch.conditionExpr)}) {
                    |${backend.translateSource(branch.block).prependIndent("    ")}
                    |}
                    """.trimMargin()
                } + if (item.elseBlock != null) {
                    """
                    | else {
                    | ${backend.translateSource(item.elseBlock).prependIndent("    ")}
                    |}
                    """.trimMargin()
                } else ""

                blockLowerings.joinToString(separator = "")
            }
            is CyanFunctionCall -> {
                val functionName = item.functionIdentifier.value.let {
                    if (it == "print" || it == "err")
                        backend.nameForBuiltin(it)
                    else it
                }

                "$functionName(${item.args.joinToString(", ", transform = backend::lowerExpression)});"
            }
            else -> error("js: cannot lower statement of type ${item::class.simpleName}")
        }
    }

}
