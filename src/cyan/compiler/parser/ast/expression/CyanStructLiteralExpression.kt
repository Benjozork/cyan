package cyan.compiler.parser.ast.expression

class CyanStructLiteralExpression (
    val exprs: Array<CyanExpression>
) : CyanExpression {

    override fun toString() = "{ ${exprs.joinToString(", ")} }"

}
