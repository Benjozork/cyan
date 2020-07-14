package cyan.compiler.fir

class FirDocument (
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope {

    override fun allReferences(): Set<String> {
        TODO("Not yet implemented")
    }

}
