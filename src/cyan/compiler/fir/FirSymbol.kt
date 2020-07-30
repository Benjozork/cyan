package cyan.compiler.fir

interface FirSymbol : FirNode {

    val name: String

    fun makeResolvedRef(parentNode: FirNode) = FirResolvedReference(parentNode, this, name)

}
