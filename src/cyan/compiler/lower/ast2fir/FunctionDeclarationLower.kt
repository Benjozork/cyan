package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionReceiver
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object FunctionDeclarationLower : Ast2FirLower<CyanFunctionDeclaration, FirNullNode> {

    override fun lower(astNode: CyanFunctionDeclaration, parentFirNode: FirNode): FirNullNode {
        val firFunctionDeclaration = FirFunctionDeclaration (
            parent = parentFirNode,
            name = astNode.signature.name.value,
            returnType = parentFirNode.resolveType(astNode.signature.typeAnnotation),
            isExtern = astNode.signature.isExtern,
            args = emptyArray()
        )

        // Resolve types for AST arguments and assign them to FIR func declaration
        firFunctionDeclaration.args = astNode.signature.args
                .map { FirFunctionArgument(firFunctionDeclaration, it.name, firFunctionDeclaration.resolveType(it.typeAnnotation, astNode)) }
                .toTypedArray()

        // Resolve type for AST receiver and add a `this` symbol if needed
        astNode.signature.receiver?.let { receiver ->
            firFunctionDeclaration.receiver = FirFunctionReceiver(firFunctionDeclaration, firFunctionDeclaration.resolveType(receiver.type))
        }

        // Lower AST function body
        firFunctionDeclaration.block = astNode.source?.let { SourceLower.lower(it, firFunctionDeclaration) } ?: FirSource(firFunctionDeclaration, isInheriting = false)

        // Check function has body if not extern
        if (!astNode.signature.isExtern && astNode.source == null) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Only extern functions can have no body",
                    astNode = astNode,
                    span = astNode.span
                )
            )
        }

        // Check parent is scope
        if (parentFirNode !is FirScope) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "FirVariableDeclaration found in ${parentFirNode::class.simpleName}",
                    astNode = astNode
                )
            )
        }

        // Check function not already declared
        if (parentFirNode.findSymbol(FirReference(parentFirNode, firFunctionDeclaration.name, CyanIdentifierExpression(firFunctionDeclaration.name))) != null) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Symbol '${firFunctionDeclaration.name}' already declared in scope",
                    astNode = astNode
                )
            )
        }

        // Register function in parent FIR node

        parentFirNode.declaredSymbols += firFunctionDeclaration

        return FirNullNode
    }

}
