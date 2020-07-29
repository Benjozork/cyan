package cyan.compiler.common.diagnostic

import cyan.compiler.common.exception.AbortedCompilationException

import com.andreapivetta.kolor.*
import cyan.compiler.common.Span
import kotlin.math.absoluteValue

object DiagnosticPipe {

    fun report(diagnostic: CompilerDiagnostic): Nothing {
        print (
            when (diagnostic.level) {
                CompilerDiagnostic.Level.Warn -> "warn: ".yellow()
                CompilerDiagnostic.Level.Error -> "err: ".lightRed()
                CompilerDiagnostic.Level.Internal -> "fatal (internal compiler error): ".lightRed()
            }
        )

        println(diagnostic.message.let { if (diagnostic.level == CompilerDiagnostic.Level.Warn) it.yellow() else it.red() })

        val span: Span? = diagnostic.span

        val prefix = if (span != null)
            " ${span.line} | ".lightGray()
        else
            "    | ".lightGray()

        println(diagnostic.astNode.toString().prependIndent(prefix))

        if (span != null) {
            val beginArrow = span.position.first - diagnostic.astNode.span!!.position.first
            println(" ".repeat(diagnostic.span.line.toString().length + 4 + beginArrow) + "~".lightRed().repeat((span.position.last - span.position.first).coerceAtLeast(0)))
        }

        if (diagnostic.note != null) {
            println("note: ".lightBlue() + diagnostic.note.message.blue())
            if (diagnostic.note.astNode != null)
                println(diagnostic.note.astNode.toString().prependIndent("    | ".lightGray()))
            println()
        } else println()

        throw AbortedCompilationException("compilation stopped because of diagnostic")
    }

}
