package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirModuleRoot
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirNullNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.lower.ModuleLoader
import cyan.compiler.parser.ast.CyanImportStatement

object ImportStatementLower : Ast2FirLower<CyanImportStatement, FirNullNode> {

    override fun lower(astNode: CyanImportStatement, parentFirNode: FirNode): FirNullNode {
        val moduleRef = FirReference(parentFirNode, astNode.moduleIdentifier.value, astNode.moduleIdentifier)
        val containingModule = parentFirNode.firstAncestorOfType<FirModuleRoot>()
            ?: DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "CyanImportStatement found in ${parentFirNode::class.simpleName}",
                    astNode = astNode
                )
            )
        val resolvedModule = ModuleLoader.findModuleByReference(moduleRef)
            ?: DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Unresolved module '${moduleRef.text}'",
                    astNode = astNode,
                    span = astNode.moduleIdentifier.span
                )
            )

        containingModule.declaredSymbols += resolvedModule.declaredSymbols

        return FirNullNode
    }

}
