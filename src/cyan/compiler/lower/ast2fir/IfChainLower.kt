package cyan.compiler.lower.ast2fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanIfChain
import cyan.compiler.parser.ast.CyanType

object IfChainLower : Ast2FirLower<CyanIfChain, FirIfChain> {

    override fun lower(astNode: CyanIfChain, parentFirNode: FirNode): FirIfChain {
        val firBranches = astNode.ifStatements.map { branch ->
            val firBranchExpr = ExpressionLower.lower(branch.conditionExpr, parentFirNode)
            val firBranchSource = SourceLower.lower(branch.block, parentFirNode)

            require(firBranchExpr.type() == Type(CyanType.Bool, false)) { "if statement condition must be a boolean expression" }

            firBranchExpr to firBranchSource
        }

        val firElseSource = astNode.elseBlock?.let { SourceLower.lower(it, parentFirNode) }

        return FirIfChain(parentFirNode, firBranches, firElseSource)
    }

}
