package cyan.compiler.mir.containers

import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class ModuleInfoContainer(override val container: Module) : MirSection {

    override val name = "module_info"

}
