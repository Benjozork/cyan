package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.ItemLower
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

object JsExpressionLower : ItemLower<CyanExpression> {

    override fun lower(backend: CompilerBackend, item: CyanExpression): String {
        return when (item) {
            is CyanStringLiteralExpression ->
                "\'${item.value}\'"
            is CyanNumericLiteralExpression ->
                item.value.toString()
            is CyanIdentifierExpression ->
                item.value
            is CyanBinaryExpression ->
                item.toString()
            is CyanArrayExpression ->
                "[${item.exprs.joinToString(", ") { backend.expressionLower.lower(backend, it) }}]"
            is CyanMemberAccessExpression ->
                "${item.base}.${item.member}"
            else -> error("js: cannot lower expression { $item } of type ${item::class.simpleName}")
        }
    }

}
