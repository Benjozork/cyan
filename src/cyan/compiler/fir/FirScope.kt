package cyan.compiler.fir

interface FirScope : FirNode {

    val declaredSymbols: MutableSet<FirSymbol>

    val localFunctions: MutableSet<FirFunctionDeclaration>

}
