package cyan.compiler.common.types

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.fir.functions.FirFunctionArgument

@Suppress("EqualsOrHashCode")
sealed class Type(val array: Boolean) {

    class Primitive(val base: CyanType, array: Boolean = false): Type(array) {

        constructor(base: CyanType): this(base, false)

        override fun toString() = base.toString().toLowerCase() + if (array) "[]" else ""

        override infix fun accepts(other: Type) =
            when (other) {
                is Struct -> {
                    base == CyanType.Any && array == other.array
                }
                is Primitive -> {
                    if (base == CyanType.Any) true
                    else base == other.base && array == other.array
                }
                else -> false
            }

        override fun asArrayType() = Primitive(base, true)

        override fun asNonArrayType() = Primitive(base, false)

        override fun hashCode() = base.hashCode() + array.hashCode()

    }

    interface Complex

    class Struct(val name: String, val properties: Array<Property>, val derives: MutableList<Derive>, array: Boolean = false) : Type(array), Complex {

        data class Property(val name: String, val type: Type) {
            override fun toString() = "$name: $type"
        }

        override fun toString() = "struct $name { ${properties.joinToString(", ")} }"

        override fun accepts(other: Type) =
            when (other) {
                is Struct -> name == other.name
                else -> false
            }

        override fun asArrayType() = Struct(name, properties, derives, true)

        override fun asNonArrayType() = Struct(name, properties, derives, false)

        override fun hashCode() = name.hashCode() + properties.hashCode() + array.hashCode()

    }

    class Trait(val name: String, val elements: Array<Element>, array: Boolean = false) : Type(array), Complex {

        sealed class Element(val name: String, val returnType: Type) {

            class Function(name: String, val args: Array<FirFunctionArgument>, returnType: Type) : Element(name, returnType)

            class Property(name: String, returnType: Type) : Element(name, returnType)

        }

        override fun toString() = "trait $name"

        override fun accepts(other: Type): Boolean =
            when (other) {
                is Struct -> this in other.derives.map { it.trait }
                is Trait -> false
                is Self -> false
                is Primitive -> false
            }

        override fun asArrayType() = Trait(name, elements, true)

        override fun asNonArrayType() = Trait(name, elements, false)

        override fun hashCode() = name.hashCode() + elements.hashCode() + array.hashCode()

    }

    class Self(array: Boolean = false) : Type(array) {

        /**
         * Resolves what type this `self` type refers to when present in a certain FIR node
         *
         * The type can only be resolves if this node has a [FirTypeDeclaration.Struct] as ancestor.
         *
         * @param firNode the [FirNode] that has this type
         */
        fun resolveIn(firNode: FirNode): Struct {
            val containingStructDeclaration = firNode.firstAncestorOfType<FirTypeDeclaration.Struct>()
                ?: error("cannot resolve 'self' type outside of a struct declaration")

            return containingStructDeclaration.type
        }

        override fun toString() = "self"

        override fun accepts(other: Type): Boolean {
            error("cannot statically check 'self' type")
        }

        override fun asArrayType() = Self(true)

        override fun asNonArrayType() = Self(false)

        override fun hashCode() = array.hashCode()

    }

    abstract infix fun accepts(other: Type): Boolean

    abstract fun asArrayType(): Type

    abstract fun asNonArrayType(): Type

    override fun equals(other: Any?) = other.hashCode() == hashCode()

    abstract override fun hashCode(): Int

}
