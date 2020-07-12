package cyan.compiler.parser.items.expression

data class CyanIdentifierExpression(val value: String): CyanExpression {
    override fun toString() = value
}
