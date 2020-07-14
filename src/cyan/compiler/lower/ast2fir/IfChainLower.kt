package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanIfChain

object IfChainLower : Ast2FirLower<CyanIfChain, FirIfChain> {

    override fun lower(astNode: CyanIfChain, parentFirNode: FirNode): FirIfChain {
        val firBranches = astNode.ifStatements.map { branch ->
            val firBranchExpr = ExpressionLower.lower(branch.conditionExpr, parentFirNode)
            val firBranchSource = SourceLower.lower(branch.block, parentFirNode)

            firBranchExpr to firBranchSource
        }

        val firElseSource = astNode.elseBlock?.let { SourceLower.lower(it, parentFirNode) }

        return FirIfChain(parentFirNode, firBranches, firElseSource)
    }

}
