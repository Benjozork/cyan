package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirAssignment
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.parser.ast.CyanAssignment

object AssignLower : Ast2FirLower<CyanAssignment, FirAssignment> {

    override fun lower(astNode: CyanAssignment, parentFirNode: FirNode): FirAssignment {
        val assignment = FirAssignment(parentFirNode)

        val variableReference = FirReference(assignment, astNode.reference.value)
        val resolvedReference = parentFirNode.findSymbol(variableReference) // Check symbol exists
            ?: DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Unresolved symbol '${variableReference.text}'",
                    astNode = astNode
                )
            )

        val resolvedSymbol = resolvedReference.resolvedSymbol

        if (resolvedSymbol !is FirVariableDeclaration) { // Check symbol is variable
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Can only assign to a variable",
                    astNode = astNode
                )
            )
        }

        if (!resolvedSymbol.mutable) { // Check variable is mutable
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Cannot assign to '${resolvedSymbol.name}' because it is immutable",
                    astNode = astNode,
                    note = CompilerDiagnostic.Note("use 'var' instead of 'let' to declare '${resolvedSymbol.name}' mutable")
                )
            )
        }

        val newExpr = FirExpression(assignment, astNode.newExpr)

        if (!(resolvedSymbol.initializationExpr.type() accepts newExpr.type())) { // Check variable accepts our type
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type mismatch: expected '${resolvedSymbol.initializationExpr.type()}', found '${newExpr.type()}'",
                    astNode = astNode
                )
            )
        }

        assignment.targetVariable = resolvedSymbol
        assignment.newExpr = newExpr

        return assignment
    }

}
