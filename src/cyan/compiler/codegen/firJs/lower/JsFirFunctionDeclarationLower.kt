package cyan.compiler.codegen.firJs.lower

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.FirItemLower
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object JsFirFunctionDeclarationLower : FirItemLower<FirFunctionDeclaration> {

    override fun lower(backend: FirCompilerBackend, item: FirFunctionDeclaration): String {
        return """
        |function ${item.name}(${item.args.joinToString(transform = FirFunctionArgument::name)}) {
        |${backend.translateSource(item.block).prependIndent("    ")}
        |}
        """.trimMargin()
    }

}
