package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirSource

interface FirOptimizationPass {

    fun run(source: FirSource)

}
