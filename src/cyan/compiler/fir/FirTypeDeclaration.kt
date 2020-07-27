package cyan.compiler.fir

import cyan.compiler.common.types.Type

class FirTypeDeclaration (
    override val parent: FirNode,
    override val name: String,
    val struct: Type.Struct
): FirStatement, FirSymbol {

    override fun toString() = struct.toString()

    override fun allReferredSymbols() = emptySet<FirSymbol>()

}
