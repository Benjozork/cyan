package cyan.compiler.fir

/**
 * Used for signaling that an AST lowering has produced no FIR nodes
 */
object FirNullNode : FirNode {

    override fun allReferences() = emptySet<String>()

}
