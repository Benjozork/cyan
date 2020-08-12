package cyan

import cyan.compiler.fir.FirModule
import cyan.compiler.fir.FirNullNode
import cyan.compiler.codegen.wasm.WasmCompilerBackend
import cyan.compiler.codegen.wasm.WasmLoweringContext

import java.io.File

import com.andreapivetta.kolor.lightGray
import com.andreapivetta.kolor.lightMagenta

fun main() {
//    val file = File("runtime/example.cy")
//
//    val mainModule = FirModule.compileModuleFromFile(file)
//
//    PureVariableInlinePass.run(mainModule.source)
//    ConstantFoldingPass.run(mainModule.source)
//    DeadBranchPass.run(mainModule.source)
//    DeadVariablePass.run(mainModule.source)
//    PureVariableInlinePass.run(mainModule.source)
//    ConstantFoldingPass.run(mainModule.source)
//    DeadBranchPass.run(mainModule.source)
//
//    mainModule.source.statements.removeAll { it is FirNullNode }

    val file = File("runtime/simple.cy")

    val simpleModule = FirModule.compileModuleFromFile(file)

    simpleModule.source.statements.removeAll { it is FirNullNode }

    println("Emitting ".lightMagenta() + "(wasm)".lightGray() + "\t'${file.name}'")
    val wasmSource = WasmCompilerBackend().let { it.translateSource(simpleModule.source, WasmLoweringContext(it), true) }

    val outputfile = File("runtime/test.wat")

    println("\n\t+ " + outputfile.path.lightGray())

    outputfile.writeText(wasmSource)
}
