package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionDeclaration

interface FirScope : FirNode {

    val declaredSymbols: MutableSet<FirSymbol>

    val localFunctions: MutableSet<FirFunctionDeclaration>

}
