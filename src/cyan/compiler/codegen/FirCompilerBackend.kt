package cyan.compiler.codegen

import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration

import java.lang.StringBuilder

abstract class FirCompilerBackend {

    abstract val prelude: String

    abstract val statementLower:  FirItemLower<FirStatement>
    abstract val expressionLower: FirItemLower<FirExpression>
    abstract val functionDeclarationLower: FirItemLower<FirFunctionDeclaration>

    abstract fun nameForBuiltin(builtinName: String): String

    fun translateSource(source: FirSource, isRoot: Boolean = false): String {
        val newSource = if (isRoot) StringBuilder(prelude + "\n") else StringBuilder()

        for (function in source.localFunctions) {
            newSource.appendln(functionDeclarationLower.lower(this, function) + "\n")
        }

        for (statement in source.statements) {
            newSource.appendln(statementLower.lower(this, statement))
        }

        return newSource.toString().removeSuffix("\n")
    }

    fun lowerStatement(stmt: FirStatement): String = statementLower.lower(this, stmt)
    fun lowerExpression(expr: FirExpression): String = expressionLower.lower(this, expr)

}
