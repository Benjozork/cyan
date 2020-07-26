package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirAssignment (
    override val parent: FirNode,
    var targetVariable: FirVariableDeclaration? = null,
    var newExpr: FirExpression? = null
) : FirStatement {

    override fun allReferredSymbols() = if (targetVariable != null && newExpr != null) {
        setOf(targetVariable!!) + newExpr!!.allReferredSymbols()
    } else error("FirSymbol::allReferredSymbols should not be accessed during node initialization")

}
