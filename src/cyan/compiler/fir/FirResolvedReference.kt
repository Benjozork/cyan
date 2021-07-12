package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.expression.CyanExpression

class FirResolvedReference(parent: FirNode, val resolvedSymbol: FirSymbol, val text: String, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode) {

    override fun allReferredSymbols() = setOf(this)

    fun reference() = FirReference(parent, text, fromAstNode)

}
