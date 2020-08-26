package cyan.compiler.mir

import cyan.compiler.mir.containers.*

class Module (

    val infoContainer: ModuleInfoContainer,

    val imports: ImportsContainer,

    val exports: ExportsContainer,

    val types: TypeDeclarationsContainer,

    val functions: FunctionsContainer,

    val derives: DerivesContainer

)
