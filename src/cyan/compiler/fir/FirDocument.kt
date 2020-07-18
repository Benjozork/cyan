package cyan.compiler.fir

import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.parser.ast.CyanType

class FirDocument (
    declaredSymbols: MutableSet<FirSymbol> = mutableSetOf(),
    override val localFunctions: MutableSet<FirFunctionDeclaration> = mutableSetOf()
) : FirScope {

    override val declaredSymbols = declaredSymbols.apply { // Intrinsics
        this += FirFunctionDeclaration (
            parent = this@FirDocument,
            name = "print",
            args = arrayOf(FirFunctionArgument(this@FirDocument, "a", FirTypeAnnotation(CyanType.Str, false)))
        )
    }

    override val parent: FirNode? get() = null

    override fun allReferredSymbols(): Set<FirSymbol> {
        TODO("Not yet implemented")
    }

}
