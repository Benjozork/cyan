package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.expression.FirExpression

object DeadBranchPass : FirOptimizationPass {

    private fun FirIfChain.trim() {
        for (branch in branches) {
            val firstBranchIsConstant = branch.first.realExpr is FirExpression.Literal.Boolean

            if (firstBranchIsConstant) {
                // Check branch expr
                val exprValue = (branch.first.realExpr as FirExpression.Literal.Boolean).value

                if (exprValue) {
                    this.replaceWith(branch.second.statements)
                    return
                } else branches = branches.filterNot { it == branch }
            }
        }

        if (elseBranch != null && branches.isEmpty())
            this.replaceWith(elseBranch!!.statements)
    }

    override fun run(source: FirSource) {
        val ifChains = source.statements.filterIsInstance<FirIfChain>()

        ifChains.forEach {
            it.trim()
            if (it.branches.isEmpty() && it.elseBranch == null)
                it.delete()
        }
    }

}
