package cyan.compiler.parser.ast

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.expression.FirExpression

class FirWhileStatement (
    override val parent: FirNode,
    val conditionExpr: FirExpression,
    val block: FirSource
) : FirStatement {

    override fun allReferredSymbols() = conditionExpr.allReferredSymbols() + block.allReferredSymbols()

}
