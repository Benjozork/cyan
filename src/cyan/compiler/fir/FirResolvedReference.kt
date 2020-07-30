package cyan.compiler.fir

class FirResolvedReference(parent: FirNode, val resolvedSymbol: FirSymbol, text: String) : FirReference(parent, text) {

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
