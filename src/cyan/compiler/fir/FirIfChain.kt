package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirIfChain(val branches: List<Pair<FirExpression, FirSource>>, val elseBranch: FirSource?) : FirStatement {

    override fun allReferences() =
        (branches.flatMap { it.first.allReferences() + it.second.allReferences() } + (elseBranch?.allReferences() ?: emptySet())).toSet()

}
