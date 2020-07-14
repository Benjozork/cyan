package cyan.compiler.fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.parser.ast.expression.CyanExpression

class FirExpression(override val parent: FirNode, val astExpr: CyanExpression) : FirNode {

    override fun allReferences(): Set<FirReference> = setOf(FirReference(this, "<not yet implemented>"))

}
