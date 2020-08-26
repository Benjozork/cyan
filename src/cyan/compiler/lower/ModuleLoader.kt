package cyan.compiler.lower

import cyan.compiler.fir.FirModuleRoot
import cyan.compiler.fir.FirReference
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.compiler.parser.CyanModuleParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import com.andreapivetta.kolor.lightGreen
import cyan.compiler.fir.FirNullNode
import cyan.compiler.mir.Module

import java.io.File

object ModuleLoader {

    val moduleParser = CyanModuleParser()

    val cachedModules = mutableMapOf<String, FirModuleRoot>()

    val runtimeModule get() = cachedModules["__runtime__"]

    fun findModuleByReference(reference: FirReference): FirModuleRoot? {
        val moduleInCache = cachedModules[reference.text]

        return if (moduleInCache != null) moduleInCache else {
            val moduleFileInCompilerResources = File("resources/runtime/${reference.text}.cy").takeIf { it.exists() }
                ?: File("resources/runtime/stdlib/${reference.text}.cy").takeIf { it.exists() }

            val loadedModule = moduleFileInCompilerResources?.let { compileModuleFromFile(it) }

            loadedModule?.let { cachedModules[reference.text] = loadedModule }

            loadedModule
        }
    }

    fun compileModuleFromFile(it: File): FirModuleRoot {
        println("Compiling".lightGreen() + "\t\t'${it.name}'")

        val moduleText = it.readText()
        val parsedModule = moduleParser.parseToEnd(moduleText)

        val moduleName = parsedModule.declaration.name.value

        // Create the MIR container

        val mirContainer = Module(name = moduleName)

        // Compile the actual source

        val compiledModule = FirModuleRoot(mirContainer)

        if (moduleName != "__runtime__")
            compiledModule.declaredSymbols += runtimeModule?.declaredSymbols ?: emptySet()

        compiledModule.source = SourceLower.lower(parsedModule.source, compiledModule)

        return compiledModule.also { it.declaredSymbols += compiledModule.source.declaredSymbols }
    }

    init {
        val runtimeModuleFile = File("resources/runtime/stdlib/runtime.cy").takeIf { it.exists() } ?: error("fatal: could not find runtime.cy module")

        cachedModules["__runtime__"] = compileModuleFromFile(runtimeModuleFile)
    }

}
