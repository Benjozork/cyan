package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanImportStatement(val moduleIdentifier: CyanIdentifierExpression, override val span: Span? = null) : CyanStatement {

    override fun toString() = "import $moduleIdentifier"

}
