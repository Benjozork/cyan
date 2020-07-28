package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanImportStatement(val moduleIdentifier: CyanIdentifierExpression) : CyanStatement {

    override fun toString() = "import $moduleIdentifier"

}
