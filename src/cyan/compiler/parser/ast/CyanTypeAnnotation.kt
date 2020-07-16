package cyan.compiler.parser.ast

class CyanTypeAnnotation(val base: CyanType, val array: Boolean) : CyanItem {
    override fun toString() = base.name.toLowerCase() + if (array) "[]" else ""
}
