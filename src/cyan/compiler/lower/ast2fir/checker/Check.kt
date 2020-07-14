package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode

interface Check<TFirNode : FirNode> {

    fun check(firNode: TFirNode, containingNode: FirNode): Boolean

}
