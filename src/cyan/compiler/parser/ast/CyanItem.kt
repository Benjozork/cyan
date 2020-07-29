package cyan.compiler.parser.ast

import cyan.compiler.common.Span

interface CyanItem {
    val span: Span
}
