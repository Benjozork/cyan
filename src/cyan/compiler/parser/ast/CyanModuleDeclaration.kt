package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanModuleDeclaration(val name: CyanIdentifierExpression) : CyanStatement {

    override fun toString() = "module $name"

}
