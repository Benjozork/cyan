package cyan.compiler.codegen.firJs

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.firJs.lower.JsFirExpressionLower
import cyan.compiler.codegen.firJs.lower.JsFirFunctionDeclarationLower
import cyan.compiler.codegen.firJs.lower.JsFirStatementLower

import java.io.File

class JsFirCompilerBackend : FirCompilerBackend() {

    override val prelude = File("runtime/runtime.js").readText()

    override val statementLower           = JsFirStatementLower
    override val expressionLower          = JsFirExpressionLower
    override val functionDeclarationLower = JsFirFunctionDeclarationLower

    override fun nameForBuiltin(builtinName: String) = "builtins.$builtinName"

}
