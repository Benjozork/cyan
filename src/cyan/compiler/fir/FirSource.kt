package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

class FirSource (
    override val parent: FirNode,
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf(),
    val statements: MutableList<FirStatement> = mutableListOf()
) : FirScope {

    override fun allReferredSymbols() = statements.flatMap { it.allReferredSymbols() }.toSet()

}
