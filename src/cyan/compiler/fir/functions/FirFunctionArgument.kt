package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirSymbol

class FirFunctionArgument (
    override val parent: FirNode,
    override val name: String,
    val typeAnnotation: Type
) : FirSymbol {

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
