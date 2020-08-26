package cyan.compiler.mir.containers

import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.mir.MirSection

class FunctionsContainer(val functions: Set<FirFunctionDeclaration>) : MirSection {

    override val name = "functions"

}
