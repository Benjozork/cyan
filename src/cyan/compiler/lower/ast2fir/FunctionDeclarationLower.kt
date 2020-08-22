package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Derive
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionReceiver
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object FunctionDeclarationLower : Ast2FirLower<CyanFunctionDeclaration, FirFunctionDeclaration> {

    override fun lower(astNode: CyanFunctionDeclaration, parentFirNode: FirNode): FirFunctionDeclaration {
        val functionDeclarationName =
            (if (parentFirNode is Derive) "__traitimpl__${parentFirNode.trait.name}_${(parentFirNode.onType as Type.Struct).name}_" else "") + astNode.signature.name.value

        val firFunctionDeclaration = FirFunctionDeclaration (
            parent = parentFirNode,
            name = functionDeclarationName,
            returnType = parentFirNode.resolveType(astNode.signature.typeAnnotation),
            isExtern = astNode.signature.isExtern,
            args = emptyArray()
        )

        // Add attributes
        firFunctionDeclaration.attributes += astNode.signature.attributes
                .map { FirReference(firFunctionDeclaration, it.ident.value, it.ident) }
                .map { FirFunctionDeclaration.Attribute(it) }

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
        if (!astNode.signature.isExtern && parentFirNode !is FirTypeDeclaration.Trait && astNode.source == null) {
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
        if (parentFirNode !is FirScope && parentFirNode !is Derive) {
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

        if (parentFirNode is FirScope)
            parentFirNode.declaredSymbols += firFunctionDeclaration

        return firFunctionDeclaration
    }

}
