package cyan.compiler.parser.ast.expression

class CyanArrayExpression(val exprs: Array<CyanExpression>) : CyanExpression {
    override fun toString() = "[${exprs.joinToString(", ")}]"
}
