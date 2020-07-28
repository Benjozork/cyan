package cyan.compiler.codegen.js

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.js.lower.JsExpressionLower
import cyan.compiler.codegen.js.lower.JsFunctionDeclarationLower
import cyan.compiler.codegen.js.lower.JsStatementLower
import cyan.compiler.fir.FirSource

import java.io.File

class JsCompilerBackend(private val stdLibSource: FirSource) : FirCompilerBackend() {

    override val prelude get() = File("runtime/runtime.js").readText()
        .replace("// CYANC_INSERT_STDLIB_HERE", translateSource(stdLibSource).removeSuffix("\n"))

    override val postlude = ""

    override val statementLower           = JsStatementLower
    override val expressionLower          = JsExpressionLower
    override val functionDeclarationLower = JsFunctionDeclarationLower

    override fun nameForBuiltin(builtinName: String) = "builtins.$builtinName"

}
