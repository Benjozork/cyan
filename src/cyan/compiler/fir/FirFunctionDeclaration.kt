package cyan.compiler.fir

class FirFunctionDeclaration(val name: String, val args: Array<String>): FirStatement {

    lateinit var block: FirSource

    override fun allReferences() = block.allReferences()

}
