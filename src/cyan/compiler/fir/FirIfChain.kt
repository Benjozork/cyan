package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirIfChain(override val parent: FirNode, val branches: List<Pair<FirExpression, FirSource>>, val elseBranch: FirSource?) : FirStatement {

    override fun allReferredSymbols() =
        (branches.flatMap { it.first.allReferredSymbols() + it.second.allReferredSymbols() } + (elseBranch?.allReferredSymbols() ?: emptySet())).toSet()

}
