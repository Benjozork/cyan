package cyan.compiler.fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration

class FirForStatement (
    override val parent: FirNode,
    val iteratorExpr: FirExpression,
) : FirStatement, FirScope {

    class IteratorVariable (
        override val parent: FirNode,
        name: String,
        iteratorType: Type
    ) : FirVariableDeclaration(parent, name, false, iteratorType.asNonArrayType())

    override val isInheriting = true

    override val declaredSymbols = mutableSetOf<FirSymbol>()

    override val localFunctions: MutableSet<FirFunctionDeclaration>
        get() = TODO("Not yet implemented")

    lateinit var block: FirSource

    override fun allReferredSymbols() = iteratorExpr.allReferredSymbols() + block.allReferredSymbols()

}
