package cyan.compiler.codegen.js

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsStatementLower

import java.io.File

class JsCompilerBackend : CompilerBackend() {

    override val prelude = File("runtime/runtime.js").readText()

    override val statementLower  = JsStatementLower
    override val expressionLower = JsExpressionLower

    override fun nameForBuiltin(builtinName: String) = "builtins.$builtinName"

}
