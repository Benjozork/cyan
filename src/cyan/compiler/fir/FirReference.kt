package cyan.compiler.fir

class FirReference(override val parent: FirNode, val text: String) : FirNode {

    override fun allReferredSymbols() = emptySet<FirSymbol>()

}
