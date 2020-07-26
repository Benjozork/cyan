package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanType
import cyan.compiler.parser.ast.CyanVariableDeclaration

object VariableDeclarationLower : Ast2FirLower<CyanVariableDeclaration, FirVariableDeclaration> {

    override fun lower(astNode: CyanVariableDeclaration, parentFirNode: FirNode): FirVariableDeclaration {
        val typeAnnotation = astNode.type?.let { Type(it.base, it.array) }

        val firVariableDeclaration = FirVariableDeclaration (
            parent = parentFirNode,
            name = astNode.name.value,
            typeAnnotation = typeAnnotation,
            initializationExpr = ExpressionLower.lower(astNode.value, parentFirNode)
        )

        // Run type-checks in initialization expression
        firVariableDeclaration.initializationExpr.type()

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

        // Check type isn't void
        if (typeAnnotation?.base == CyanType.Void) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Variables cannot have 'void' type",
                    astNode = astNode
                )
            )
        }

        // Check type annotation, if present, accepts initialization expr type
        if (typeAnnotation != null && !(typeAnnotation accepts firVariableDeclaration.initializationExpr.type())) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type mismatch: expected '${firVariableDeclaration.typeAnnotation}', found '${firVariableDeclaration.initializationExpr.type()}'",
                    astNode = astNode
                )
            )
        }

        // Check variable not already declared
        if (parentFirNode.findSymbol(FirReference(parentFirNode, firVariableDeclaration.name)) != null) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Symbol '${firVariableDeclaration.name}' already declared in scope",
                    astNode = astNode
                )
            )
        }

        parentFirNode.declaredSymbols += firVariableDeclaration

        return firVariableDeclaration
    }

}
