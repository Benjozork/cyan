package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

open class FirAssignment (
    override val parent: FirNode
) : FirStatement {

    lateinit var targetVariable: FirVariableDeclaration

    lateinit var newExpr: FirExpression

    class ToArrayIndex(parent: FirNode, val indexExpr: FirExpression) : FirAssignment(parent)

    override fun allReferredSymbols() = setOf(targetVariable.makeResolvedRef(this)) + newExpr.allReferredSymbols()

}
