package cyan.compiler.codegen.js

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.LoweringContext
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsFunctionDeclarationLower
import cyan.compiler.codegen.js.lower.JsStatementLower
import cyan.compiler.fir.FirSource

import java.io.File

class JsCompilerBackend : FirCompilerBackend<String>() {

    val prelude get() = File("resources/runtime/runtime.js").readText()

    val postlude = ""

    override fun makeLoweringContext() = JsLoweringContext(this)

    override val statementLower           = JsStatementLower
    override val expressionLower          = JsExpressionLower
    override val functionDeclarationLower = JsFunctionDeclarationLower

    override fun translateSource(source: FirSource, context: LoweringContext, isRoot: Boolean): String {
        TODO("Not yet implemented")
    }

}
