package cyan.compiler.parser.ast.expression

data class CyanIdentifierExpression(val value: String): CyanExpression {
    override fun toString() = value
}
