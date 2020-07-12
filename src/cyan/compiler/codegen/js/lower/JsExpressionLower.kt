package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.CompilerBackend
import cyan.compiler.codegen.ItemLower
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

object JsExpressionLower : ItemLower<CyanExpression> {

    override fun lower(backend: CompilerBackend, item: CyanExpression): String {
        return when (item) {
            is CyanStringLiteralExpression -> "\'${item.value}\'"
            is CyanNumericLiteralExpression -> item.value.toString()
            is CyanBinaryExpression -> item.toString()
            is CyanIdentifierExpression -> item.value
            else -> error("js: cannot lower expression { $item } of type ${item::class.simpleName}")
        }
    }

}
