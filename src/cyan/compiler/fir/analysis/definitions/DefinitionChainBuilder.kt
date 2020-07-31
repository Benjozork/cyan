package cyan.compiler.fir.analysis.definitions

import cyan.compiler.fir.FirAssignment
import cyan.compiler.fir.analysis.cfg.CfgNode

object DefinitionChainBuilder {

    private fun CfgNode.findAssignmentUsages(assignment: FirAssignment, accumulator: MutableList<CfgNode>): List<CfgNode> {
        if (fromFirNode.allReferredSymbols().any { it.resolvedSymbol == assignment.targetVariable })
            accumulator += this

        return when (this) {
            is CfgNode.Forwarding -> this.nextNode?.findAssignmentUsages(assignment, accumulator) ?: return accumulator.toList()
            is CfgNode.Conditional -> {
                val usagesInTrueBranch = trueNode!!.findAssignmentUsages(assignment, accumulator)
                val usagesInFalseBranch = falseNode?.findAssignmentUsages(assignment, accumulator) ?: emptyList()

                usagesInTrueBranch + usagesInFalseBranch
             }
        }
    }

    fun build(baseCfgNode: CfgNode): List<CfgNode> {
        require(baseCfgNode.fromFirNode is FirAssignment) { "definition-chain-builder: FIR node of base CFG node must be a FirAssignment" }

        return baseCfgNode.findAssignmentUsages(baseCfgNode.fromFirNode as FirAssignment, mutableListOf())
    }

}
