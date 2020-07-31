package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirIfChain(override val parent: FirNode) : FirStatement {

    var branches: List<Pair<FirExpression, FirSource>> = emptyList()
    var elseBranch: FirSource? = null

    override fun allReferredSymbols() =
        (branches.flatMap { it.first.allReferredSymbols() + it.second.allReferredSymbols() } + (elseBranch?.allReferredSymbols() ?: emptySet())).toSet()

}
