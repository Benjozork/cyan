package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

interface FirScope : FirNode {

    val isInheriting: Boolean

    val declaredSymbols: MutableSet<FirSymbol>

}
