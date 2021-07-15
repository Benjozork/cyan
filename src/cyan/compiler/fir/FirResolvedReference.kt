package cyan.compiler.fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.functions.FirFunctionReceiver
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class FirResolvedReference(parent: FirNode, val resolvedSymbol: FirSymbol, val text: String, fromAstNode: CyanExpression) : FirExpression(parent, fromAstNode) {

    fun reference() = FirReference(parent, text, fromAstNode)

    override fun type(): Type = when (val referee = resolvedSymbol) {
        is FirForStatement.IteratorVariable -> referee.typeAnnotation!!
        is FirVariableDeclaration -> referee.initializationExpr.type()
        is FirFunctionDeclaration -> {
            val functionStruct = findSymbol(FirReference(referee, "Function", CyanIdentifierExpression("Function")))
                ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Internal,
                        astNode = this.fromAstNode,
                        message = "Could not find 'Function' struct in scope",
                        span = this.fromAstNode.span,
                    )
                )

            if (functionStruct.resolvedSymbol !is FirTypeDeclaration.Struct) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Internal,
                        astNode = this.fromAstNode,
                        message = "Found 'Function' in scope, but it was not a struct declaration",
                        span = this.fromAstNode.span,
                    )
                )
            }

            functionStruct.resolvedSymbol.type
        }
        is FirFunctionArgument -> referee.typeAnnotation
        is FirFunctionReceiver -> referee.type
        else -> error("can't infer type of ${referee::class.simpleName}")
    }

    override fun allReferredSymbols() = setOf(this)

}
