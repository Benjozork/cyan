package cyan.compiler.mir.containers

import cyan.compiler.common.types.Derive
import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class DerivesContainer(override val container: Module) : MirSection {

    val deriveItems: MutableSet<Derive> = mutableSetOf()

    override val name = "derives"

}
