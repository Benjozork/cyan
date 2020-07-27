package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object NoNamedFunctionClosures : Check<FirFunctionDeclaration> {

    override fun check(firNode: FirFunctionDeclaration, containingNode: FirNode): Boolean {
        require (containingNode is FirScope) { "should never happen" }

        val functionReferences = firNode.allReferredSymbols()
        val symbolsDeclaredInScope = containingNode.declaredSymbols
        val functionArguments = firNode.args

        return functionReferences.any {
            it in symbolsDeclaredInScope &&
                    it.name !in functionArguments.map(FirFunctionArgument::name)
        }
    }

}