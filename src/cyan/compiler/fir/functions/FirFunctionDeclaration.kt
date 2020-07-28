package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirSymbol

class FirFunctionDeclaration (
    override val parent: FirNode,
    override val name: String,
    val returnType: Type,
    val isExtern: Boolean,
    var args: Array<FirFunctionArgument>
): FirScope, FirSymbol {

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

    override val declaredSymbols by lazy { (args.map { it as FirSymbol } + this).toMutableSet() }

    override val localFunctions = mutableSetOf<FirFunctionDeclaration>()

}
