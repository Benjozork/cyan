package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirIfChain
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanIfChain

object IfChainLower : Ast2FirLower<CyanIfChain, FirIfChain> {

    override fun lower(astNode: CyanIfChain): FirIfChain {
        val firBranches = astNode.ifStatements.map { branch ->
            val firBranchExpr = ExpressionLower.lower(branch.conditionExpr)
            val firBranchSource = SourceLower.lower(branch.block)

            firBranchExpr to firBranchSource
        }

        val firElseSource = astNode.elseBlock?.let { SourceLower.lower(it) }

        return FirIfChain(firBranches, firElseSource)
    }

}
