package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.ItemLower
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.CyanVariableDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object JsStatementLower : ItemLower<CyanStatement> {

    override fun lower(backend: CompilerBackend, item: CyanStatement): String {
        return when (item) {
            is CyanVariableDeclaration -> "const ${item.name} = ${backend.expressionLower.lower(backend, item.value)};"
            is CyanFunctionCall -> "${item.functionIdentifier.value}(${item.args.joinToString(", ") { backend.expressionLower.lower(backend, it) }});"
            is CyanFunctionDeclaration -> {
                """
                |function ${item.signature.name}(${item.signature.args.joinToString { it.value }}) {
                |${backend.translateSource(item.source).prependIndent("    ")}
                |}
                """.trimMargin()
            }
            else -> error("js: cannot lower statement of type ${item::class.simpleName}")
        }
    }

}
