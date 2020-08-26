package cyan.compiler.mir.containers

import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.mir.MirSection

class TypeDeclarationsContainer(val types: Set<FirTypeDeclaration<*>>) : MirSection {

    override val name = "type_declarations"

}
