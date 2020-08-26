package cyan.compiler.mir.containers

import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.mir.MirSection
import cyan.compiler.mir.Module

class TypeDeclarationsContainer(override val container: Module) : MirSection {

    val types = mutableSetOf<FirTypeDeclaration<*>>()

    override val name = "type_declarations"

}
