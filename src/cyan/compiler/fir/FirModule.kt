package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.compiler.parser.CyanModuleParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import java.io.File

class FirModule (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf()
) : FirScope {

    override val localFunctions = declaredSymbols.filterIsInstance<FirFunctionDeclaration>().toMutableSet()

    override val parent: FirNode? get() = null

    override fun allReferredSymbols(): Set<FirSymbol> {
        TODO("Not yet implemented")
    }

    fun findModuleByReference(reference: FirReference): FirModule? {
        val moduleFileInCompilerResources = File("runtime/${reference.text}.cy").takeIf { it.exists() }

        val loadedModule = moduleFileInCompilerResources?.let {
            val moduleText = it.readText()
            val parsedModule = CyanModuleParser().parseToEnd(moduleText)
            val compiledModule = FirModule()

            SourceLower.lower(parsedModule.source, compiledModule).also { fir -> compiledModule.declaredSymbols += fir.declaredSymbols }

            compiledModule
        }

        return loadedModule
    }

}
