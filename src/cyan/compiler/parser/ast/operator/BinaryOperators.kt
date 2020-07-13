package cyan.compiler.parser.ast.operator

object CyanBinaryPlusOperator : CyanBinaryOperator {
    override fun toString() = "+"
}

object CyanBinaryMinusOperator : CyanBinaryOperator {
    override fun toString() = "-"
}

object CyanBinaryTimesOperator : CyanBinaryOperator {
    override fun toString() = "*"
}

object CyanBinaryDivOperator : CyanBinaryOperator {
    override fun toString() = "/"
}

object CyanBinaryModOperator : CyanBinaryOperator {
    override fun toString() = "%"
}

object CyanBinaryExpOperator : CyanBinaryOperator {
    override fun toString() = "^"
}

object CyanBinaryAndOperator : CyanBinaryOperator {
    override fun toString() = "&&"
}

object CyanBinaryOrOperator : CyanBinaryOperator {
    override fun toString() = "||"
}
