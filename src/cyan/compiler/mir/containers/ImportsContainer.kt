package cyan.compiler.mir.containers

import cyan.compiler.fir.FirSymbol
import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class ImportsContainer(override val container: Module) : MirSection {

    val importedSymbols = mutableSetOf<FirSymbol>()

    override val name = "imports"

}
