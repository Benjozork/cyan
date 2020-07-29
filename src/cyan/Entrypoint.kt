package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.lower.ast2fir.optim.DeadVariablePass

import java.io.File

fun main() {
    val mainModule = FirModule.compileModuleFromFile(File("runtime/example.cy"))

    DeadVariablePass.run(mainModule.source)

    val jsSource = JsCompilerBackend().translateSource(mainModule.source, isRoot = true)

    println(jsSource)
}
