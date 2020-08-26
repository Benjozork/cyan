package cyan.compiler.fir

import cyan.compiler.mir.Module

class FirModuleRoot(val mirModule: Module) : FirScope {

    override val declaredSymbols get() = (
        mirModule.types.typeDeclarations +
        mirModule.imports.importedSymbols +
        mirModule.functions.functionDeclarations
    ).toMutableSet()

    override val isInheriting = false

    lateinit var source: FirSource

    override val parent: FirNode? get() = null

    override fun allReferredSymbols() = source.allReferredSymbols()

}
