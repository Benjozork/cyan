package cyan.compiler.parser.ast

import cyan.compiler.parser.ast.expression.CyanExpression

class CyanReturn(val expr: CyanExpression): CyanStatement {
    override fun toString() = "return $expr"
}
