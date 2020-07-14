package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirVariableDeclaration(override val name: String, val initializationExpr: FirExpression) : FirStatement, FirSymbol {

    override fun allReferences() = initializationExpr.allReferences()

}
