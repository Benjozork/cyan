package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirSymbol

class FirFunctionReceiver(override val parent: FirNode, val type: Type) : FirSymbol {

    override val name = "this"

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
