package cyan.compiler.codegen.wasm.dsl

object Wasm {

    interface OrderedElement {

        override fun toString(): String

    }

    class Instruction(val text: String) : OrderedElement {

        override fun toString() = text

    }

}
