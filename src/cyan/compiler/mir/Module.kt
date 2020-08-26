package cyan.compiler.mir

import cyan.compiler.mir.containers.*

class Module(val name: String) {

    val infoContainer = ModuleInfoContainer(this)

    val imports = ImportsContainer(this)

    var exports = ExportsContainer(this)

    var types = TypeDeclarationsContainer(this)

    var functions = FunctionsContainer(this)

    var derives = DerivesContainer(this)

}
