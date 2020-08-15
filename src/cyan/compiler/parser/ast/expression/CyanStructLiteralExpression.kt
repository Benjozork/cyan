package cyan.compiler.parser.ast.expression

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanStructLiteralExpression (
    val exprs: Array<CyanExpression>,
    val typeAnnotation: CyanTypeAnnotation.Reference? = null,
    override val span: Span? = null
) : CyanExpression {

    override fun toString() = "{ ${exprs.joinToString(", ")} }"

}
