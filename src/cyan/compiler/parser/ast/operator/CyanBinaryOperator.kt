package cyan.compiler.parser.ast.operator

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem

interface CyanBinaryOperator : CyanItem {

    override val span: Span get() = TODO()

}
