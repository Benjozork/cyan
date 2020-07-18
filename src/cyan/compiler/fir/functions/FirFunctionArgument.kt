package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSymbol
import cyan.compiler.fir.FirTypeAnnotation

class FirFunctionArgument (
    override val parent: FirNode,
    override val name: String,
    val typeAnnotation: FirTypeAnnotation
) : FirSymbol {

    override fun allReferredSymbols() = emptySet<FirSymbol>()

}
