package cyan.compiler.fir

interface FirScope : FirNode {

    val localFunctions: MutableSet<FirFunctionDeclaration>

}
