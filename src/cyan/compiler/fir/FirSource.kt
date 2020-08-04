package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

open class FirSource (
    override var parent: FirNode,
    override val declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    var statements: MutableList<FirStatement> = mutableListOf()
) : FirScope {

    override val localFunctions get() = declaredSymbols.filterIsInstance<FirFunctionDeclaration>().toMutableSet()

    override fun allReferredSymbols() = statements.flatMap { it.allReferredSymbols() }.toSet()

    fun handleDeletionOfChild(child: FirStatement) {
        statements.removeAll { it == child }
        declaredSymbols.removeAll { it == child }
    }

    fun handleReplacementOfChild(child: FirStatement, newStatements: List<FirStatement>) {
        statements = statements.flatMap { if (it == child) newStatements else listOf(it) }.toMutableList()
    }

    class Inheriting (
        parent: FirNode,
        declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
        statements: MutableList<FirStatement> = mutableListOf()
    ) : FirSource(parent, declaredSymbols, statements) {

        override val declaredSymbols: MutableSet<FirSymbol>
            get() = if (parent.parent is FirScope)
                ((parent.parent as FirScope).declaredSymbols + super.declaredSymbols).toMutableSet()
            else super.declaredSymbols

    }

}
