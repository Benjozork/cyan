package cyan.compiler.fir

interface FirNode {

    val parent: FirNode?

    fun allReferredSymbols(): Set<FirSymbol>

}
