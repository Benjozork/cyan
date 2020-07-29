package cyan.compiler.parser.ast

import cyan.compiler.common.Span

class CyanSource(val statements: List<CyanStatement>, override val span: Span): CyanItem
