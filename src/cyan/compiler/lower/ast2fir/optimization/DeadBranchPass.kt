package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSource
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression

object DeadBranchPass : FirOptimizationPass {

    private fun FirIfChain.eliminateImpossibleBranches(): FirNode { // Very bad and needs fixing
        this.branches = this.branches.filterNot { b -> b.first.isConstant && b.first.astExpr is CyanBooleanLiteralExpression && !(b.first.astExpr as CyanBooleanLiteralExpression).value }

        if (this.branches.isEmpty() && this.elseBranch != null)
            this.replaceWith(this.elseBranch.statements)

        return this
    }

    override fun run(source: FirSource) {
        val ifChains = source.statements.filterIsInstance<FirIfChain>()

        ifChains.forEach { it.eliminateImpossibleBranches() }
    }

}
