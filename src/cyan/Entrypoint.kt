package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.fir.FirNullNode
import cyan.compiler.lower.ast2fir.optimization.*

import java.io.File

fun main() {
    val mainModule = FirModule.compileModuleFromFile(File("runtime/example.cy"))

    PureVariableInlinePass.run(mainModule.source)
    ConstantFoldingPass.run(mainModule.source)
    DeadBranchPass.run(mainModule.source)
    DeadVariablePass.run(mainModule.source)
    PureVariableInlinePass.run(mainModule.source)
    ConstantFoldingPass.run(mainModule.source)
    DeadBranchPass.run(mainModule.source)

    mainModule.source.statements.removeAll { it is FirNullNode }

//    SsaPass.run(mainModule.source)

//    println(mainModule.allReferredSymbols().joinToString("\n") { "ref to symbol '${it.resolvedSymbol.name}' in a fir node of type ${it.parent::class.simpleName}" })

//    val jsSource = JsCompilerBackend().translateSource(mainModule.source, isRoot = true)
//
//    println(jsSource)

    val simpleModule = FirModule.compileModuleFromFile(File("runtime/simple.cy"))

    simpleModule.source.statements.removeAll { it is FirNullNode }

    val wasmSource = WasmCompilerBackend().let { it.translateSource(simpleModule.source, WasmLoweringContext(it), true) }

    val outputfile = File("runtime/test.wat")

    outputfile.writeText(wasmSource)
}
