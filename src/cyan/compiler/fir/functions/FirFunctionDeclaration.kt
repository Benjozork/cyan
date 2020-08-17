package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.*

class FirFunctionDeclaration (
    override val parent: FirNode,
    override val name: String,
    val returnType: Type,
    val isExtern: Boolean,
    var args: Array<FirFunctionArgument>
): FirScope, FirSymbol {

    override val isInheriting = false

    var receiver: FirFunctionReceiver? = null

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

    override val declaredSymbols get() = (args.toList() + this + receiver).filterNotNull().toMutableSet()

    override val localFunctions = mutableSetOf<FirFunctionDeclaration>()

}
