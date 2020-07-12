package cyan.compiler.parser.ast.expression

class CyanMemberAccessExpression(val base: CyanExpression, val member: CyanIdentifierExpression) : CyanExpression {
    override fun toString() = "$base.$member"
}
