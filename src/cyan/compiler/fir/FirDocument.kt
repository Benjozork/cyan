package cyan.compiler.fir

class FirDocument(override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()) : FirScope
