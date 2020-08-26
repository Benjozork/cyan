package cyan.compiler.mir.containers

import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class FunctionsContainer(override val container: Module) : MirSection {

    val functionDeclarations = mutableSetOf<FirFunctionDeclaration>()

    override val name = "functions"

}
