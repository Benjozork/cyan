package cyan.compiler.fir

import cyan.compiler.common.types.Derive
import cyan.compiler.common.types.Type

sealed class FirTypeDeclaration<TType : Type> (
    override val parent: FirNode
): FirStatement, FirSymbol {

    abstract val type: TType

    override val name: String
        get() = when (val type = type) {
            is Type.Struct -> type.name
            is Type.Trait -> type.name
            is Type.Primitive -> type.toString()
            else -> error("invalid state")
        }

    class Struct(parent: FirNode, override val type: Type.Struct) : FirTypeDeclaration<Type.Struct>(parent) {

        lateinit var derives: Set<Derive>

    }

    class Trait(parent: FirNode) : FirTypeDeclaration<Type.Trait>(parent), FirScope {

        override lateinit var type: Type.Trait

        override val isInheriting = false

        override val declaredSymbols = mutableSetOf<FirSymbol>()

    }

    override fun toString() = type.toString()

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
