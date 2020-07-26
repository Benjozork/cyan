package cyan.compiler.fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.expression.FirExpression

class FirVariableDeclaration (
    override val parent: FirNode,
    override val name: String,
    val mutable: Boolean,
    val typeAnnotation: Type?,
    val initializationExpr: FirExpression
) : FirStatement, FirSymbol {

    override fun allReferredSymbols() = initializationExpr.allReferredSymbols()

}
