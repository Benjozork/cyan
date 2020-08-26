package cyan.compiler.mir.containers

import cyan.compiler.fir.FirSymbol
import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class ExportsContainer(override val container: Module) : MirSection {

    val exportedSymbols = mutableSetOf<FirSymbol>()

    override val name = "exports"

}

