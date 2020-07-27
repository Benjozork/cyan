package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanIfChain
import cyan.compiler.common.types.CyanType

object IfChainLower : Ast2FirLower<CyanIfChain, FirIfChain> {

    override fun lower(astNode: CyanIfChain, parentFirNode: FirNode): FirIfChain {
        val firBranches = astNode.ifStatements.map { branch ->
            val firBranchExpr = ExpressionLower.lower(branch.conditionExpr, parentFirNode)
            val firBranchSource = SourceLower.lower(branch.block, parentFirNode)

            if (firBranchExpr.type() != Type.Primitive(CyanType.Bool, false)) {
               DiagnosticPipe.report (
                   CompilerDiagnostic (
                       level = CompilerDiagnostic.Level.Error,
                       message = "if statement condition must be a boolean expression",
                       astNode = branch
                   )
               )
            }

            firBranchExpr to firBranchSource
        }

        val firElseSource = astNode.elseBlock?.let { SourceLower.lower(it, parentFirNode) }

        return FirIfChain(parentFirNode, firBranches, firElseSource)
    }

}
