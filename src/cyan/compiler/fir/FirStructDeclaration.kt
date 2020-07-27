package cyan.compiler.fir

import cyan.compiler.common.types.Type

class FirStructDeclaration (
    override val parent: FirNode,
    override val name: String,
    var properties: Array<Property>? = emptyArray()
): FirStatement, FirSymbol {

    override fun allReferredSymbols() = emptySet<FirSymbol>()

    class Property(override val parent: FirNode, override val name: String, val type: Type): FirSymbol {

        override fun allReferredSymbols() = emptySet<FirSymbol>()

    }

}
