package cyan.compiler.fir.analysis.cfg

import cyan.compiler.fir.FirNullNode
import cyan.compiler.fir.FirSource

object StatementChainCfgBuilder {

    fun build(source: FirSource): CfgNode.Forwarding {
        val baseNode = CfgNode.Forwarding().apply { fromFirNode = FirNullNode }

        var previousNode: CfgNode = baseNode
        for ((index, statement) in source.statements.withIndex()) {
            val newNode = StatementCfgBuilder.build(statement, previousNode)

            when (previousNode) {
                is CfgNode.Forwarding -> previousNode.nextNode = newNode
                is CfgNode.Conditional -> {
                    val exitNode = CfgNode.Exit().apply { fromFirNode = FirNullNode }

                    previousNode.exitNode = exitNode
                }
            }

            if (index == source.statements.size - 1 && newNode is CfgNode.Conditional) {
                val exitNode = CfgNode.Exit().apply { fromFirNode = FirNullNode }

                newNode.exitNode = exitNode
            }

            previousNode = newNode
        }

        return baseNode
    }

}
