package cyan.compiler.fir

import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanExpression

open class FirReference(override val parent: FirNode, val text: String, val fromAstNode: CyanExpression) : FirNode {

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
