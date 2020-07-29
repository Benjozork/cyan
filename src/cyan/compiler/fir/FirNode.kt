package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.CyanExpression

interface FirNode {

    val parent: FirNode?

    fun allReferredSymbols(): Set<FirSymbol>

    fun makeChildExpr(expr: CyanExpression) = FirExpression(this, expr)

}
