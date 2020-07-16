package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirVariableDeclaration (
    override val parent: FirNode,
    override val name: String,
    val typeAnnotation: FirTypeAnnotation?,
    val initializationExpr: FirExpression
) : FirStatement, FirSymbol {

    override fun allReferredSymbols() = initializationExpr.allReferredSymbols()

}
