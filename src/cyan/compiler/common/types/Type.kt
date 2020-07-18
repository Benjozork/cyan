package cyan.compiler.common.types

import cyan.compiler.parser.ast.CyanType

data class Type (
    val base: CyanType,
    val array: Boolean
) {

    override fun toString() = base.toString().toLowerCase() + if (array) "[]" else ""

    infix fun accepts(other: Type) =
        if (base == CyanType.Any) true
        else base == other.base && array == other.array

}
