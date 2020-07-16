package cyan.compiler.fir

/**
 * Used for signaling that an AST lowering has produced no FIR nodes
 */
object FirNullNode : FirNode {

    override val parent: FirNode? get() = null

    override fun allReferredSymbols() = emptySet<FirSymbol>()

}
