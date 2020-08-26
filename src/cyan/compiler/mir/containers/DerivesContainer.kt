package cyan.compiler.mir.containers

import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class DerivesContainer(override val container: Module) : MirSection {

    override val name = "derives"

}
