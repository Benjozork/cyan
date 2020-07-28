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

interface CyanBinaryComparisonOperator : CyanBinaryOperator

object CyanBinaryEqualsOperator : CyanBinaryComparisonOperator {
    override fun toString() = "=="
}

object CyanBinaryNotEqualsOperator : CyanBinaryComparisonOperator {
    override fun toString() = "!="
}

object CyanBinaryLesserOperator : CyanBinaryComparisonOperator {
    override fun toString() = "<"
}

object CyanBinaryLesserEqualsOperator : CyanBinaryComparisonOperator {
    override fun toString() = "<="
}

object CyanBinaryGreaterOperator : CyanBinaryComparisonOperator {
    override fun toString() = ">"
}

object CyanBinaryGreaterEqualsOperator : CyanBinaryComparisonOperator {
    override fun toString() = ">="
}

object CyanBinaryAndOperator : CyanBinaryComparisonOperator {
    override fun toString() = "&&"
}

object CyanBinaryOrOperator : CyanBinaryComparisonOperator {
    override fun toString() = "||"
}
