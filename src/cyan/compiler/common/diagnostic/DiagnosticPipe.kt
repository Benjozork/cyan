package cyan.compiler.common.diagnostic

import com.andreapivetta.kolor.*

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
        println("   | ".lightGray() + diagnostic.astNode + "\n")

        error("compilation stopped because of diagnostic")
    }

}
