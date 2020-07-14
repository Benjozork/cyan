package cyan.compiler.fir

interface FirScope : FirNode {

    val declaredSymbols: MutableSet<String>

    val localFunctions: MutableSet<FirFunctionDeclaration>

}
