package cyan.compiler.parser.ast.function

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanExpression

data class CyanFunctionCall(val functionName: CyanIdentifierExpression, val args: Array<CyanExpression>): CyanStatement {
    override fun toString() = "$functionName(${args.joinToString(", ")})"
}
