package cyan.compiler.codegen

import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanExpression

import java.lang.StringBuilder

abstract class CompilerBackend {

    abstract val prelude: String

    abstract val statementLower:  ItemLower<CyanStatement>
    abstract val expressionLower: ItemLower<CyanExpression>

    abstract fun nameForBuiltin(builtinName: String): String

    fun translateSource(source: CyanSource, isRoot: Boolean = false): String {
        val newSource = if (isRoot) StringBuilder(prelude + "\n") else StringBuilder()

        for (statement in source.statements) {
            newSource.appendln(statementLower.lower(this, statement))
        }

        return newSource.toString().removeSuffix("\n")
    }

    fun lowerStatement(stmt: CyanStatement): String = statementLower.lower(this, stmt)
    fun lowerExpression(expr: CyanExpression): String = expressionLower.lower(this, expr)

}
