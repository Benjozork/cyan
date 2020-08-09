package cyan.compiler.codegen

import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration

abstract class FirCompilerBackend<TLowerOutput : Any> {

    abstract val statementLower:  FirItemLower<*, FirStatement, TLowerOutput>
    abstract val expressionLower: FirItemLower<*, FirExpression, TLowerOutput>
    abstract val functionDeclarationLower: FirItemLower<*, FirFunctionDeclaration, TLowerOutput>

    abstract fun translateSource(source: FirSource, context: LoweringContext, isRoot: Boolean = false): String

    abstract fun makeLoweringContext(): LoweringContext

    @Suppress("UNCHECKED_CAST")
    open fun lowerFunctionDeclaration(function: FirFunctionDeclaration): TLowerOutput =
        (functionDeclarationLower as FirItemLower<LoweringContext, FirFunctionDeclaration, TLowerOutput>).lower(makeLoweringContext(), function)

    @Suppress("UNCHECKED_CAST")
    open fun lowerStatement(stmt: FirStatement, context: LoweringContext): TLowerOutput =
        (statementLower as FirItemLower<LoweringContext, FirStatement, TLowerOutput>).lower(context, stmt)

    @Suppress("UNCHECKED_CAST")
    open fun lowerExpression(expr: FirExpression, context: LoweringContext): TLowerOutput =
        (expressionLower as FirItemLower<LoweringContext, FirExpression, TLowerOutput>).lower(context, expr)

}
