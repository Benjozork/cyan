package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirSymbol
import cyan.compiler.parser.ast.function.CyanFunctionArgument

class FirFunctionArgument (
    override val parent: FirNode,
    override val name: String,
    val typeAnnotation: Type,
    val fromAstNode: CyanFunctionArgument? = null,
) : FirSymbol {

    override fun allReferredSymbols() = emptySet<FirResolvedReference>()

}
