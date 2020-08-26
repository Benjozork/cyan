package cyan.compiler.mir.containers

import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class ExportsContainer(override val container: Module) : MirSection {

    override val name = "exports"

}

