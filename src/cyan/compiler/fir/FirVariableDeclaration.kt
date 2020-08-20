package cyan.compiler.fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.expression.FirExpression

open class FirVariableDeclaration (
    override val parent: FirNode,
    override val name: String,
    val mutable: Boolean,
    val typeAnnotation: Type?
) : FirStatement, FirSymbol {

    lateinit var initializationExpr: FirExpression

    override fun allReferredSymbols() = initializationExpr.allReferredSymbols()

}
