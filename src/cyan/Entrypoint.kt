package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.fir.FirNullNode
import cyan.compiler.lower.ast2fir.optimization.ConstantFoldingPass
import cyan.compiler.lower.ast2fir.optimization.DeadBranchPass
import cyan.compiler.lower.ast2fir.optimization.DeadVariablePass
import cyan.compiler.lower.ast2fir.optimization.PureVariableInlinePass

import java.io.File

fun main() {
    val mainModule = FirModule.compileModuleFromFile(File("runtime/example.cy"))

    ConstantFoldingPass.run(mainModule.source)
    PureVariableInlinePass.run(mainModule.source)
    DeadBranchPass.run(mainModule.source)
    DeadVariablePass.run(mainModule.source)

    mainModule.source.statements.removeAll { it is FirNullNode }

    println(mainModule.allReferredSymbols().joinToString("\n") { "ref to symbol '${it.resolvedSymbol.name}' in a fir node of type ${it.parent::class.simpleName}" })

    val jsSource = JsCompilerBackend().translateSource(mainModule.source, isRoot = true)

    println(jsSource)
}
