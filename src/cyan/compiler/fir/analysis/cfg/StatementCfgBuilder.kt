package cyan.compiler.fir.analysis.cfg

import cyan.compiler.fir.FirAssignment
import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.functions.FirFunctionCall

object StatementCfgBuilder {

    fun build(statement: FirStatement, previousCfgNode: CfgNode): CfgNode {
        return when (statement) {
            is FirVariableDeclaration,
            is FirFunctionCall,
            is FirAssignment -> CfgNode.Forwarding()
            is FirIfChain -> ConditionalCfgBuilder.build(statement)
            else -> error("cfg-builder: cannot build cfg node for statement of type '${statement::class.simpleName}'")
        }.apply { fromFirNode = statement }
    }

}
