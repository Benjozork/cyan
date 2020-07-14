package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirFunctionDeclaration
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSymbol

object NoNamedFunctionClosures : Check<FirFunctionDeclaration> {

    override fun check(firNode: FirFunctionDeclaration, containingNode: FirNode): Boolean {
        require (containingNode is FirScope) { "should never happen" }

        val functionReferences = firNode.allReferences()
        val symbolsDeclaredInScope = containingNode.declaredSymbols.map(FirSymbol::name)
        val functionArguments = firNode.args

        return functionReferences.any { it.text in symbolsDeclaredInScope && it.text !in functionArguments }
    }

}
