package cyan.compiler.fir

import cyan.compiler.fir.expression.FirExpression

class FirVariableDeclaration(val name: String, val initializationExpr: FirExpression) : FirStatement {

    override fun allReferences() = initializationExpr.allReferences()

}
