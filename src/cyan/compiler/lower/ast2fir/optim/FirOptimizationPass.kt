package cyan.compiler.lower.ast2fir.optim

import cyan.compiler.fir.FirSource

interface FirOptimizationPass {

    fun run(source: FirSource)

}
