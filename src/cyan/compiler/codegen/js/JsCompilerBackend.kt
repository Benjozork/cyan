package cyan.compiler.codegen.js

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.LoweringContext
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsFunctionDeclarationLower
import cyan.compiler.codegen.js.lower.JsStatementLower

import java.io.File

class JsCompilerBackend : FirCompilerBackend<String>() {

    override val prelude get() = File("runtime/runtime.js").readText()

    override val postlude = ""

    override fun makeLoweringContext() = JsLoweringContext(this)

    override val statementLower           = JsStatementLower
    override val expressionLower          = JsExpressionLower
    override val functionDeclarationLower = JsFunctionDeclarationLower

}
