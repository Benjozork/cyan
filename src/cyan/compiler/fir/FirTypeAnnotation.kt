package cyan.compiler.fir

import cyan.compiler.parser.ast.CyanType

data class FirTypeAnnotation(val base: CyanType, val array: Boolean) {
    override fun toString() = "$base" + if (array) "[]" else ""
}
