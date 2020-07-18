package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirSymbol

class FirFunctionDeclaration(override val parent: FirNode, override val name: String, var args: Array<FirFunctionArgument>): FirSymbol {

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

}
