package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration

object DeadVariablePass : FirOptimizationPass {

    override fun run(source: FirSource) {
        val allVariables = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>().filter { it.initializationExpr.isPure }
        val unusedVariables = allVariables.filter { source.allReferredSymbols().none { s -> s.resolvedSymbol == it } }

        unusedVariables.forEach { it.delete() }
    }

}
