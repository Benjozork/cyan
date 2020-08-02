package cyan.compiler.codegen.js.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.codegen.js.JsLoweringContext
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object JsFunctionDeclarationLower : FirItemLower<JsCompilerBackend, JsLoweringContext, FirFunctionDeclaration> {

    override fun lower(context: JsLoweringContext, item: FirFunctionDeclaration): String {
        return """
        |function ${item.name}(${item.args.joinToString(transform = FirFunctionArgument::name)}) {
        |${context.backend.translateSource(item.block).prependIndent("    ")}
        |}
        """.trimMargin()
    }

}
