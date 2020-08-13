package cyan.compiler.fir.functions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.expression.FirExpression

interface FirCall : FirNode {

    val callee: FirResolvedReference

    val args: Array<FirExpression>

}
