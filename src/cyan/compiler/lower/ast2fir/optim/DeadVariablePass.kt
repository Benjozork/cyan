package cyan.compiler.lower.ast2fir.optim

import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration

object DeadVariablePass : FirOptimizationPass {

    override fun run(source: FirSource) {
        val allVariables = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>().filterNot { it.initializationExpr.isComplex() }
        val unusedVariables = allVariables.filter { source.allReferredSymbols().none { s -> s == it } }

        unusedVariables.forEach { it.delete() }
    }

}
