package cyan.compiler.fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.functions.FirFunctionDeclaration

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

    class Struct(parent: FirNode, override val type: Type.Struct) : FirTypeDeclaration<Type.Struct>(parent)

    class Trait(parent: FirNode) : FirTypeDeclaration<Type.Trait>(parent), FirScope {

        override lateinit var type: Type.Trait

        override val isInheriting = false

        override val declaredSymbols = mutableSetOf<FirSymbol>()

        override val localFunctions: MutableSet<FirFunctionDeclaration>
            get() = TODO("Not yet implemented")

    }

    override fun toString() = type.toString()

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
