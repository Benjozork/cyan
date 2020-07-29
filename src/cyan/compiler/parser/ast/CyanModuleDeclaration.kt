package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanModuleDeclaration(val name: CyanIdentifierExpression, override val span: Span) : CyanStatement {

    override fun toString() = "module $name"

}
