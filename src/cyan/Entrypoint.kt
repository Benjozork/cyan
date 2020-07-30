package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.fir.FirNullNode
import cyan.compiler.lower.ast2fir.optimization.ConstantFoldingPass
import cyan.compiler.lower.ast2fir.optimization.DeadBranchPass
import cyan.compiler.lower.ast2fir.optimization.DeadVariablePass

import java.io.File

fun main() {
    val mainModule = FirModule.compileModuleFromFile(File("runtime/example.cy"))

    DeadVariablePass.run(mainModule.source)
    ConstantFoldingPass.run(mainModule.source)
    DeadBranchPass.run(mainModule.source)

    mainModule.source.statements.removeAll { it is FirNullNode }

    val jsSource = JsCompilerBackend().translateSource(mainModule.source, isRoot = true)

    println(jsSource)
}
