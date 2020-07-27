package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object JsFunctionDeclarationLower : FirItemLower<JsCompilerBackend, FirFunctionDeclaration> {

    override fun lower(backend: JsCompilerBackend, item: FirFunctionDeclaration): String {
        return """
        |function ${item.name}(${item.args.joinToString(transform = FirFunctionArgument::name)}) {
        |${backend.translateSource(item.block).prependIndent("    ")}
        |}
        """.trimMargin()
    }

}
