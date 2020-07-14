package cyan.compiler.fir

interface FirNode {

    val parent: FirNode?

    fun allReferences(): Set<FirReference>

}
