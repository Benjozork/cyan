package cyan.compiler.fir

class FirSource (
    override val parent: FirNode,
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf(),
    val statements: MutableList<FirStatement> = mutableListOf()
) : FirScope {

    override fun allReferences() = statements.flatMap { it.allReferences() }.toSet()

}
