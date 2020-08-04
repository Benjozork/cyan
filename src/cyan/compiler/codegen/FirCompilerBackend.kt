package cyan.compiler.codegen

import cyan.compiler.fir.FirModule
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration

import java.lang.StringBuilder

abstract class FirCompilerBackend {

    abstract val prelude: String
    abstract val postlude: String

    abstract val statementLower:  FirItemLower<*, *, FirStatement>
    abstract val expressionLower: FirItemLower<*, *, FirExpression>
    abstract val functionDeclarationLower: FirItemLower<*, *, FirFunctionDeclaration>

    fun translateSource(source: FirSource, context: LoweringContext, isRoot: Boolean = false): String {
        val newSource = if (isRoot) StringBuilder(prelude + "\n") else StringBuilder()

        // Here, for now we include all functions in the `declaredSymbols` of the parent module if the parent of this
        // FirSource is a module. That way, we inline functions imported from other modules. However, we need to not
        // do this if we are not the direct child of a FirModule, because then we would inline all imported functions into
        // all FirSources.
        if (source.parent is FirModule) source.parent.let { module ->
            for (function in (module as FirModule).localFunctions.filter { !it.isExtern }) {
                newSource.appendln(lowerFunctionDeclaration(function))
            }
        } else for (function in source.localFunctions) {
            newSource.appendln(lowerFunctionDeclaration(function))
        }

        for (statement in source.statements) {
            newSource.appendln(lowerStatement(statement, context))
        }

        if (isRoot) newSource.append(postlude)

        return newSource.toString().removeSuffix("\n")
    }

    abstract fun makeLoweringContext(): LoweringContext

    @Suppress("UNCHECKED_CAST")
    open fun lowerFunctionDeclaration(function: FirFunctionDeclaration): String =
        (functionDeclarationLower as FirItemLower<FirCompilerBackend, LoweringContext, FirFunctionDeclaration>).lower(makeLoweringContext(), function) + "\n"

    @Suppress("UNCHECKED_CAST")
    open fun lowerStatement(stmt: FirStatement, context: LoweringContext): String =
        (statementLower as FirItemLower<FirCompilerBackend, LoweringContext, FirStatement>).lower(context, stmt)

    @Suppress("UNCHECKED_CAST")
    open fun lowerExpression(expr: FirExpression, context: LoweringContext): String =
        (expressionLower as FirItemLower<FirCompilerBackend, LoweringContext, FirExpression>).lower(context, expr)

}
