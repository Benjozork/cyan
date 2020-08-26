package cyan.compiler.fir

class FirModuleRoot(val name: String) : FirScope {

    override val declaredSymbols = mutableSetOf<FirSymbol>()

    override val isInheriting = false

    lateinit var source: FirSource

    override val parent: FirNode? get() = null

    override fun allReferredSymbols() = source.allReferredSymbols()

}
