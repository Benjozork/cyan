package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.codegen.js.JsCompilerBackend

import java.io.File

fun main() {
    val mainModule = FirModule.compileModuleFromFile(File("runtime/example.cy"))

    val jsSource = JsCompilerBackend().translateSource(mainModule.source, isRoot = true)

    println(jsSource)
}
