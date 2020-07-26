package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

class FirDocument (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope {

    override val parent: FirNode? get() = null

    override fun allReferredSymbols(): Set<FirSymbol> {
        TODO("Not yet implemented")
    }

}
