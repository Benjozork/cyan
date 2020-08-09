package cyan.compiler.codegen.wasm.dsl

object Wasm {

    @Suppress("EnumEntryName")
    enum class Type {
        i32, i64, f32, f64;

        override fun toString() = super.toString().toLowerCase()
    }

    interface OrderedElement {

        override fun toString(): String

    }

    class Local(val name: String, val type: Type) : OrderedElement {

        override fun toString() = "(local \$$name $type)"

    }

    class Instruction(val text: String) : OrderedElement {

        override fun toString() = text

    }

}
