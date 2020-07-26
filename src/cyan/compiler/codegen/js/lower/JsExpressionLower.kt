package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.ItemLower
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

object JsExpressionLower : ItemLower<CyanExpression> {

    override fun lower(backend: CompilerBackend, item: CyanExpression): String {
        return when (item) {
            is CyanStringLiteralExpression -> {
                val escapedString = item.value.replace("'", "\\'")

                "\'$escapedString\'"
            }
            is CyanNumericLiteralExpression ->
                item.value.toString()
            is CyanBooleanLiteralExpression ->
                item.value.toString()
            is CyanIdentifierExpression ->
                item.value
            is CyanBinaryExpression ->
                item.toString()
            is CyanArrayExpression ->
                "[${item.exprs.joinToString(", ", transform = backend::lowerExpression)}]"
            is CyanMemberAccessExpression ->
                "${item.base}.${item.member}"
            is CyanArrayIndexExpression ->
                "${backend.lowerExpression(item.base)}[${item.index}]"
            else -> error("js: cannot lower expression { $item } of type ${item::class.simpleName}")
        }
    }

}
