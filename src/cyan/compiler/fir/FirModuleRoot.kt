package cyan.compiler.fir

import cyan.compiler.mir.Module

class FirModuleRoot(val mirModule: Module) : FirScope {

    override val declaredSymbols get() = mirModule.imports.importedSymbols

    override val isInheriting = false

    lateinit var source: FirSource

    override val parent: FirNode? get() = null

    override fun allReferredSymbols() = source.allReferredSymbols()

}
