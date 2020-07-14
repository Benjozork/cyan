package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirFunctionDeclaration
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSymbol

object NoNamedFunctionClosures : Check<FirFunctionDeclaration> {

    override fun check(firFunctionDeclaration: FirFunctionDeclaration, containingNode: FirNode): Boolean {
        val functionReferences = firFunctionDeclaration.allReferences()
        val symbolsDeclaredInScope = (containingNode as FirScope).declaredSymbols.map(FirSymbol::name)
        val functionArguments = firFunctionDeclaration.args

        return functionReferences.any { it in symbolsDeclaredInScope && it !in functionArguments }
    }

}
