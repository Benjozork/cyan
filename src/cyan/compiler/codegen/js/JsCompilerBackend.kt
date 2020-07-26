package cyan.compiler.codegen.js

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsFunctionDeclarationLower
import cyan.compiler.codegen.js.lower.JsStatementLower

import java.io.File

class JsCompilerBackend : FirCompilerBackend() {

    override val prelude = File("runtime/runtime.js").readText()
    override val postlude = ""

    override val statementLower           = JsStatementLower
    override val expressionLower          = JsExpressionLower
    override val functionDeclarationLower = JsFunctionDeclarationLower

    override fun nameForBuiltin(builtinName: String) = "builtins.$builtinName"

}
