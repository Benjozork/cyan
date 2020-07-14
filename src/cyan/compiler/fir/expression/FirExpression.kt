package cyan.compiler.fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class FirExpression(val astExpr: CyanExpression) : FirNode {

    override fun allReferences() = if (astExpr is CyanIdentifierExpression) setOf(astExpr.value) else emptySet()

}
