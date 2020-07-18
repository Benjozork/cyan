package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirSymbol

class FirFunctionDeclaration(override val parent: FirNode, override val name: String, var args: Array<FirFunctionArgument>): FirScope, FirSymbol {

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

    override val declaredSymbols get() = args.map { it as FirSymbol }.toMutableSet()

    override val localFunctions = mutableSetOf<FirFunctionDeclaration>()

}
