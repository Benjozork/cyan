package cyan.compiler.fir.analysis.cfg

import cyan.compiler.fir.FirIfChain

object ConditionalCfgBuilder {

    fun build(conditional: FirIfChain): CfgNode.Conditional {
        val branchCfgNodes = conditional.branches.map { StatementChainCfgBuilder.build(it.second) }
        val elseBranchCfgNode = conditional.elseBranch?.let { StatementChainCfgBuilder.build(it) }

        return CfgNode.Conditional().apply {
            trueNode = branchCfgNodes.first()
            falseNode = elseBranchCfgNode
        }
    }

}
