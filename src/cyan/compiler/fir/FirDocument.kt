package cyan.compiler.fir

class FirDocument (
    declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope {

    override val declaredSymbols = declaredSymbols.apply { // Intrinsics
        this += FirFunctionDeclaration(this@FirDocument, "print", arrayOf("a"))
    }

    override val parent: FirNode? get() = null

    override fun allReferredSymbols(): Set<FirSymbol> {
        TODO("Not yet implemented")
    }

}
