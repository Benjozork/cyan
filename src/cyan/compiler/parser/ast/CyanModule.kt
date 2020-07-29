package cyan.compiler.parser.ast

import cyan.compiler.common.Span

class CyanModule(val declaration: CyanModuleDeclaration, val source: CyanSource, override val span: Span) : CyanItem
