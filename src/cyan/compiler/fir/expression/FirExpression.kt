package cyan.compiler.fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSymbol
import cyan.compiler.parser.ast.expression.CyanExpression

class FirExpression(override val parent: FirNode, val astExpr: CyanExpression) : FirNode {

    override fun allReferredSymbols(): Set<FirSymbol> = emptySet()

}
