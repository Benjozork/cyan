package cyan.compiler.codegen.js

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsStatementLower

class JsCompilerBackend : CompilerBackend() {

    override val prelude = """
    |function print(content) {
    |   console.log(content)
    |}
    |
    """.trimMargin()

    override val statementLower  = JsStatementLower
    override val expressionLower = JsExpressionLower

}
