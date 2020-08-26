package cyan.compiler.fir

interface FirScope : FirNode {

    val isInheriting: Boolean

    val declaredSymbols: MutableSet<FirSymbol>

}
