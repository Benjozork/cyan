package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSource

object DeadBranchPass : FirOptimizationPass {

    private fun FirIfChain.eliminateDeadBranches(): FirNode {
        this.branches = this.branches.filterNot { b -> b.first.isConstant }

        if (this.branches.isEmpty() && this.elseBranch != null)
            error("branch trimming yielded only an else branch - dear maintainer; please implement inlining!")

        return this
    }

    override fun run(source: FirSource) {
        val ifChains = source.statements.filterIsInstance<FirIfChain>()

        ifChains.forEach { it.eliminateDeadBranches() }
    }

}
