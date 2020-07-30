package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.compiler.parser.CyanModuleParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import java.io.File

class FirModule (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    val name: String
) : FirScope {

    lateinit var source: FirSource

    override val localFunctions get() = declaredSymbols.filterIsInstance<FirFunctionDeclaration>().toMutableSet()

    override val parent: FirNode? get() = null

    override fun allReferredSymbols() = source.allReferredSymbols()

    fun findModuleByReference(reference: FirReference): FirModule? {
        val moduleInCache = cachedModules[reference.text]

        return if (moduleInCache != null) moduleInCache else {
            val moduleFileInCompilerResources = File("runtime/${reference.text}.cy").takeIf { it.exists() }

            val loadedModule = moduleFileInCompilerResources?.let { compileModuleFromFile(it) }

            loadedModule?.let { cachedModules[reference.text] = loadedModule }

            loadedModule
        }
    }

    companion object Loader {
        private val moduleParser = CyanModuleParser()

        private val cachedModules = mutableMapOf<String, FirModule>()

        private val runtimeModule get() = cachedModules["__runtime__"] ?: error("fatal: runtime module not in module cache")

        fun compileModuleFromFile(it: File): FirModule {
            val moduleText = it.readText()
            val parsedModule = moduleParser.parseToEnd(moduleText)

            val moduleName = parsedModule.declaration.name.value

            val compiledModule = FirModule(name = moduleName)

            if (moduleName != "__runtime__")
                compiledModule.declaredSymbols += runtimeModule.declaredSymbols

            compiledModule.source = SourceLower.lower(parsedModule.source, compiledModule)

            return compiledModule.also { it.declaredSymbols += compiledModule.source.declaredSymbols }
        }

        init {
            val runtimeModuleFile = File("runtime/runtime.cy").takeIf { it.exists() } ?: error("fatal: could not find runtime.cy module")

            cachedModules["__runtime__"] = compileModuleFromFile(runtimeModuleFile)
        }
    }

}
