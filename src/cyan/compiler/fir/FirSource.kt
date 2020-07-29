package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

class FirSource (
    override val parent: FirNode,
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    val statements: MutableList<FirStatement> = mutableListOf()
) : FirScope {

    override val localFunctions get() = declaredSymbols.filterIsInstance<FirFunctionDeclaration>().toMutableSet()

    override fun allReferredSymbols() = statements.flatMap { it.allReferredSymbols() }.toSet()

    fun handleDeletionOfChild(child: FirStatement) = statements.removeAll { it == child }.let { Unit }

}
