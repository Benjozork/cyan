package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.LoweringContext
import cyan.compiler.codegen.wasm.dsl.Wasm
import cyan.compiler.codegen.wasm.lower.WasmExpressionLower
import cyan.compiler.codegen.wasm.lower.WasmFunctionDeclarationLower
import cyan.compiler.codegen.wasm.lower.WasmStatementLower
import cyan.compiler.codegen.wasm.utils.Allocator
import cyan.compiler.fir.FirModule
import cyan.compiler.fir.FirSource

import java.io.File

import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
class WasmCompilerBackend : FirCompilerBackend<Wasm.OrderedElement>() {

    private val d = "$"
    private val n = "\n"

    private val templateText = File("runtime/runtime.wat").readText()

    override fun makeLoweringContext() = WasmLoweringContext(this)

    override val statementLower           = WasmStatementLower
    override val expressionLower          = WasmExpressionLower
    override val functionDeclarationLower = WasmFunctionDeclarationLower

    val allocator = Allocator()

    override fun translateSource(source: FirSource, context: LoweringContext, isRoot: Boolean): String {
        val newSource = StringBuilder()

        // Here, for now we include all functions in the `declaredSymbols` of the parent module if the parent of this
        // FirSource is a module. That way, we inline functions imported from other modules. However, we need to not
        // do this if we are not the direct child of a FirModule, because then we would inline all imported functions into
        // all FirSources.
        if (source.parent is FirModule) source.parent.let { module ->
            for (function in (module as FirModule).localFunctions.filter { !it.isExtern }) {
                newSource.appendln(lowerFunctionDeclaration(function))
            }
        } else for (function in source.localFunctions) {
            newSource.appendln(lowerFunctionDeclaration(function))
        }

        for (statement in source.statements) {
            newSource.appendln(lowerStatement(statement, context))
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
