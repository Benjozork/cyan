package cyan.compiler.codegen.wasm.dsl

object Wasm {

    enum class Type {
        I32, I64, F32, F64;

        override fun toString() = super.toString().toLowerCase()
    }

    interface OrderedElement {

        override fun toString(): String

    }

    class Instruction(val text: String) : OrderedElement {

        override fun toString() = text

    }

}
