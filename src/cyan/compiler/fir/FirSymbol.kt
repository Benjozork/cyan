package cyan.compiler.fir

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

interface FirSymbol : FirNode {

    val name: String

    fun makeResolvedRef(parentNode: FirNode) = FirResolvedReference(parentNode, this, name, CyanIdentifierExpression(name))

}
