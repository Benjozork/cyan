package cyan.compiler.fir

open class FirReference(override val parent: FirNode, val text: String) : FirNode {

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
