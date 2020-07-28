package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

class FirDocument (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf()
) : FirScope {

    override val localFunctions = declaredSymbols.filterIsInstance<FirFunctionDeclaration>().toMutableSet()

    override val parent: FirNode? get() = null

    override fun allReferredSymbols(): Set<FirSymbol> {
        TODO("Not yet implemented")
    }

}
