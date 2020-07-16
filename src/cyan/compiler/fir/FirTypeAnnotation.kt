package cyan.compiler.fir

import cyan.compiler.parser.ast.CyanType

data class FirTypeAnnotation(val base: CyanType, val array: Boolean)
