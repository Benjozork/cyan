package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.fir.functions.FirFunctionArgument
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

        firFunctionDeclaration.args = astNode.signature.args // Resolve types for AST arguments and assign them to FIR func declaration
                .map { FirFunctionArgument(firFunctionDeclaration, it.name, firFunctionDeclaration.resolveType(it.typeAnnotation, astNode)) }
                .toTypedArray()

        // Lower AST function body
        firFunctionDeclaration.block = astNode.source?.let { SourceLower.lower(it, firFunctionDeclaration) } ?: FirSource(firFunctionDeclaration)

        if (!astNode.signature.isExtern && astNode.source == null) { // Check function has body if not extern
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Only extern functions can have no body",
                    astNode = astNode
                )
            )
        }

        if (parentFirNode !is FirScope) { // Check parent is scope
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "FirVariableDeclaration found in ${parentFirNode::class.simpleName}",
                    astNode = astNode
                )
            )
        }

//        if (NoNamedFunctionClosures.check(firFunctionDeclaration, parentFirNode)) {
//            DiagnosticPipe.report (
//                CompilerDiagnostic (
//                    level = CompilerDiagnostic.Level.Error,
//                    message = "Function cannot be a closure (refer to symbols in it's outer scope)",
//                    astNode = astNode
//                )
//            )
//        }

        if (parentFirNode.findSymbol(FirReference(parentFirNode, firFunctionDeclaration.name)) != null) { // Check function not already declared
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
        parentFirNode.localFunctions.add(firFunctionDeclaration)

        return FirNullNode
    }

}
