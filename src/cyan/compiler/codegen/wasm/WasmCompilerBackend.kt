package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.LoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.lower.WasmExpressionLower
import cyan.compiler.codegen.wasm.lower.WasmFunctionDeclarationLower
import cyan.compiler.codegen.wasm.lower.WasmStatementLower
import cyan.compiler.codegen.wasm.utils.Allocator
import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

import java.io.File

import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
class WasmCompilerBackend : FirCompilerBackend<Wasm.OrderedElement>() {

    private val templateText = File("resources/runtime/runtime.wat").readText()

    override fun makeLoweringContext() = WasmLoweringContext(this)

    override val statementLower           = WasmStatementLower
    override val expressionLower          = WasmExpressionLower
    override val functionDeclarationLower = WasmFunctionDeclarationLower

    val allocator = Allocator()

    override fun translateSource(source: FirSource, context: LoweringContext, isRoot: Boolean): String {
        val newSource = StringBuilder()

        if (isRoot) {
            // Add call to cy_init_heap to main function
            val mainFunction = (source.parent as FirModuleRoot).declaredSymbols
                .find { it is FirFunctionDeclaration && it.name == "main" } as? FirFunctionDeclaration

            if (mainFunction != null) {
                val initHeapRef = FirReference(mainFunction, "cy_init_heap", CyanIdentifierExpression("cy_init_heap"))
                val initHeapResolved = mainFunction.findSymbol(initHeapRef)

                if (initHeapResolved != null) {
                    val call = FirExpression.FunctionCall(mainFunction, CyanFunctionCall(CyanIdentifierExpression("cy_init_heap"), emptyArray()))
                    call.callee = initHeapResolved

                    mainFunction.block.statements.add(0, call)
                } else {
                    DiagnosticPipe.report(
                        CompilerDiagnostic(
                            level = CompilerDiagnostic.Level.Internal,
                            message = "Could not resolve reference to cy_init_heap while inserting it into main. Is the intrinsics module imported?",
                            astNode = CyanIdentifierExpression("cy_init_heap"),
                        )
                    )
                }
            } else {
                DiagnosticPipe.report(
                    CompilerDiagnostic(
                        level = CompilerDiagnostic.Level.Internal,
                        message = "No main function found",
                        astNode = CyanIdentifierExpression("<nyi>"),
                    )
                )
            }
        }

        if (source.parent is FirModuleRoot) source.parent.let { module ->
            // Add declared functions
            val functionsToEmit = (module as FirModuleRoot).mirModule.functions.functionDeclarations.filter { !it.isExtern }.toMutableList()

            // Add imported functions
            functionsToEmit += module.mirModule.imports.importedSymbols.filterIsInstance<FirFunctionDeclaration>().filter { !it.isExtern }

            // Add derive function impls from declared structs
            functionsToEmit += module.mirModule.derives.deriveItems.flatMap { it.functionImpls.values }

            for (function in functionsToEmit) {
                newSource.appendLine(lowerFunctionDeclaration(function))
            }
        } else for (function in source.declaredSymbols.filterIsInstance<FirFunctionDeclaration>()) {
            newSource.appendLine(lowerFunctionDeclaration(function))
        }

        val newSourceText = newSource.toString().removeSuffix("\n")

        return if (isRoot) {
            val heapStart = (allocator.heap.size + 4 - 1) / 4 * 4

            templateText
                    .replace(";; cyanc_insert_heap_start_here", "(global \$heap_start i32 (i32.const $heapStart))")
                    .replace(";; cyanc_insert_here", newSourceText)
                    .replace(";; cyanc_insert_prealloc_here", "(data (i32.const 0) \"" + heapToByteStr() + "\")")
        } else newSourceText
    }

    private fun heapToByteStr(): String {
        fun Byte.toUnsigned(): Int = if (this < 0) (128 - abs(this.toInt())) + 128 else this.toInt()
        fun Int.padded(): String = if (this.toString(16).length == 1) "0${this.toString(16)}" else this.toString(16)

        return allocator.heap.joinToString("") { "\\${it.toUnsigned().padded()}" }
    }

}
