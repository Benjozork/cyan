package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirAssignment
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.containingScope

object PureVariableInlinePass : FirOptimizationPass {

    private fun FirVariableDeclaration.inline() {
        val references = containingScope()!!.allReferredSymbols().filter { it.resolvedSymbol == this }

        if (references.any { it.parent is FirAssignment }) return

        references.forEach {
            if (it.parent is FirExpression)
                it.parent.inlinedAstExpr = this.initializationExpr.astExpr
        }
    }

    override fun run(source: FirSource) {
        val pureVars = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>().filter { it.initializationExpr.isPure }

        pureVars.forEach { it.inline() }
    }

}
