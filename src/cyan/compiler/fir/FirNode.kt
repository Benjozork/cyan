package cyan.compiler.fir

interface FirNode {

    fun allReferences(): Set<String>

}
