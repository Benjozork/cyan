package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanVariableDeclaration

object VariableDeclarationLower : Ast2FirLower<CyanVariableDeclaration, FirVariableDeclaration> {

    override fun lower(astNode: CyanVariableDeclaration, parentFirNode: FirNode): FirVariableDeclaration {
        val typeAnnotation = astNode.type

        val firVariableDeclaration = FirVariableDeclaration (
            parent = parentFirNode,
            name = astNode.name.value,
            typeAnnotation = typeAnnotation?.let { parentFirNode.resolveType(it, astNode) },
            mutable = astNode.mutable,
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
        if (firVariableDeclaration.typeAnnotation == Type.Primitive(CyanType.Void)) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Variables cannot have 'void' type",
                    astNode = astNode
                )
            )
        }

        // Check type annotation, if present, accepts initialization expr type
        if (firVariableDeclaration.typeAnnotation != null && !(firVariableDeclaration.typeAnnotation accepts firVariableDeclaration.initializationExpr.type())) {
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type mismatch: expected '${firVariableDeclaration.typeAnnotation}', found '${firVariableDeclaration.initializationExpr.type()}'",
                    astNode = astNode,
                    note = CompilerDiagnostic.Note("inferred type '${firVariableDeclaration.initializationExpr.type()}' from initialization", astNode.value)
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
