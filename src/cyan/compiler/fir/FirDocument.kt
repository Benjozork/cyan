package cyan.compiler.fir

class FirDocument (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope {

    override val parent: FirNode? get() = null

    override fun allReferences(): Set<FirReference> {
        TODO("Not yet implemented")
    }

}
