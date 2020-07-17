package cyan.compiler.common.diagnostic

import cyan.compiler.parser.ast.CyanItem

data class CompilerDiagnostic (
    val level: Level,
    val astNode: CyanItem,
    val message: String
) {

    enum class Level {
        Warn, Error, Internal
    }

}
