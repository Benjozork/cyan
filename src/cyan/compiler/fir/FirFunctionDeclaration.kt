package cyan.compiler.fir

class FirFunctionDeclaration(override val parent: FirNode, override val name: String, val args: Array<String>): FirSymbol {

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

}
