package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirNullNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.lower.ast2fir.checker.NoNamedFunctionClosures
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object FunctionDeclarationLower : Ast2FirLower<CyanFunctionDeclaration, FirNullNode> {

    override fun lower(astNode: CyanFunctionDeclaration, parentFirNode: FirNode): FirNullNode {
        val firFunctionDeclaration = FirFunctionDeclaration (
            parent = parentFirNode,
            name = astNode.signature.name.value,
            args = emptyArray()
        )

        firFunctionDeclaration.args = astNode.signature.args
                .map { FirFunctionArgument(firFunctionDeclaration, it.name, it.typeAnnotation.let { a -> Type(a.base, a.array) }) }
                .toTypedArray()

        firFunctionDeclaration.declaredSymbols += firFunctionDeclaration.args

        firFunctionDeclaration.block = SourceLower.lower(astNode.source, firFunctionDeclaration)

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

//        if (NoNamedFunctionClosures.check(firFunctionDeclaration, parentFirNode)) {
//            DiagnosticPipe.report (
//                CompilerDiagnostic (
//                    level = CompilerDiagnostic.Level.Error,
//                    message = "Function cannot be a closure (refer to symbols in it's outer scope)",
//                    astNode = astNode
//                )
//            )
//        }

        // Check variable not already declared
        if (parentFirNode.findSymbol(FirReference(parentFirNode, firFunctionDeclaration.name)) != null) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Symbol '${firFunctionDeclaration.name}' already declared in scope",
                    astNode = astNode
                )
            )
        }

        parentFirNode.declaredSymbols += firFunctionDeclaration
        parentFirNode.localFunctions.add(firFunctionDeclaration)

        return FirNullNode
    }

}
