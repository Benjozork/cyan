package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirFunctionDeclaration
import cyan.compiler.fir.FirScope

object NoNamedFunctionClosures {

    fun check(firFunctionDeclaration: FirFunctionDeclaration, containingScope: FirScope): Boolean {
        val functionReferences = firFunctionDeclaration.allReferences()
        val symbolsDeclaredInScope = containingScope.declaredSymbols
        val functionArguments = firFunctionDeclaration.args

        return functionReferences.any { it in symbolsDeclaredInScope && it !in functionArguments }
    }

}
