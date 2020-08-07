package cyan.compiler.common.types

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

        override fun hashCode() = base.hashCode() + array.hashCode()

    }

    class Struct(val name: String, val properties: Array<Property>, array: Boolean = false) : Type(array) {

        data class Property(val name: String, val type: Type) {
            override fun toString() = "$name: $type"
        }

        override fun toString() = "struct $name { ${properties.joinToString(", ")} }"

        override fun accepts(other: Type) =
            when (other) {
                is Struct -> name == other.name
                else -> false
            }

        override fun asArrayType() = Struct(name, properties, true)

        override fun hashCode() = name.hashCode() + properties.hashCode() + array.hashCode()

    }

    abstract infix fun accepts(other: Type): Boolean

    abstract fun asArrayType(): Type

    override fun equals(other: Any?) = other.hashCode() == hashCode()

    abstract override fun hashCode(): Int

}
