package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanAssignment

object AssignLower : Ast2FirLower<CyanAssignment, FirAssignment> {

    override fun lower(astNode: CyanAssignment, parentFirNode: FirNode): FirAssignment {
        return when (val loweredBase = ExpressionLower.lower(astNode.base, parentFirNode)) {
            is FirResolvedReference -> {
                val assignment = FirAssignment(parentFirNode)

                loweredBase.parent = assignment

                val resolvedSymbol = loweredBase.resolvedSymbol

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
                            astNode = astNode, span = astNode.base.span,
                            note = CompilerDiagnostic.Note("use 'var' instead of 'let' to declare '${resolvedSymbol.name}' mutable")
                        )
                    )
                }

                val newExpr = ExpressionLower.lower(astNode.newExpr, assignment)

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

                assignment
            }
            is FirExpression.ArrayIndex -> {
                val assignment = FirAssignment.ToArrayIndex(parentFirNode, loweredBase.index)

                val baseArray = loweredBase.base

                if (baseArray !is FirResolvedReference) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "can only assign to arrays held in variables",
                        astNode = astNode, span = loweredBase.base.fromAstNode.span
                    )
                )

                val baseArrayReferee = baseArray.resolvedSymbol

                if (baseArrayReferee !is FirVariableDeclaration) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "can only assign to arrays held in variables",
                        astNode = astNode, span = loweredBase.base.fromAstNode.span
                    )
                )

                if (!baseArrayReferee.mutable) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "'${baseArrayReferee.name}' must be mutable in order to modify it",
                        astNode = astNode, span = loweredBase.base.fromAstNode.span
                    )
                )

                val baseArrayType = baseArrayReferee.initializationExpr.type().asNonArrayType()
                val newExpr = ExpressionLower.lower(astNode.newExpr, assignment)

                if (!(baseArrayType accepts newExpr.type())) { // Check variable accepts our type
                    DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "Type mismatch: expected '${baseArrayReferee.initializationExpr.type()}', found '${newExpr.type()}'",
                            astNode = astNode, span = newExpr.fromAstNode.span
                        )
                    )
                }

                assignment.targetVariable = baseArrayReferee
                assignment.newExpr = newExpr

                assignment
            }
            else -> DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "cannot assign to expression of type '${loweredBase::class.simpleName}'",
                    astNode = astNode, span = loweredBase.fromAstNode.span
                )
            )
        }
    }

}
