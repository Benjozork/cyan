package cyan.compiler.fir

class FirSource (
    val statements: MutableList<FirStatement> = mutableListOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope
