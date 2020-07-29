package cyan.compiler.common.diagnostic

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem

data class CompilerDiagnostic (
    val level: Level,
    val astNode: CyanItem,
    val message: String,
    val span: Span? = null,
    val note: Note? = null
) {

    class Note(val message: String, val astNode: CyanItem? = null)

    enum class Level {
        Warn, Error, Internal
    }

}
