package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.lower.ast2fir.expression.value.ValueInitializationConverter
import cyan.compiler.lower.ast2fir.resolve.TypeMemberResolver
import cyan.compiler.parser.ast.function.CyanFunctionCall

object FunctionCallLower : Ast2FirLower<CyanFunctionCall, FirNode> {

    override fun lower(astNode: CyanFunctionCall, parentFirNode: FirNode): FirNode {
        val firFunctionCall = FirExpression.FunctionCall(parentFirNode, astNode)

        // Are we making a receiver call or not ?
        return when (val loweredBase = ExpressionLower.lower(astNode.base, firFunctionCall)) {
            is FirResolvedReference -> { // No
                return when (val symbol = loweredBase.resolvedSymbol) {
                    is FirTypeDeclaration.Struct -> ValueInitializationConverter.convert(astNode, parentFirNode)
                    is FirFunctionDeclaration    -> processFunctionCall(firFunctionCall, symbol, astNode)
                    else -> error("Cannot invoke on symbol of type '${symbol::class.simpleName}'")
                }
            }
            is FirExpression.MemberAccess -> { // Yes
                val functionReference = FirReference(firFunctionCall, loweredBase.member, astNode.base)
                val baseType = loweredBase.base.type() as? Type.Struct

                // Find a member function or a function with the same name
                val resolvedReference = baseType?.let { TypeMemberResolver.resolve(baseType, functionReference) } ?: parentFirNode.findSymbol(functionReference) ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Unresolved symbol '${functionReference.text}'",
                        astNode = astNode, span = loweredBase.fromAstNode.span
                    )
                )

                require (resolvedReference.resolvedSymbol is FirFunctionDeclaration)

                val resolvedFunction = resolvedReference.resolvedSymbol as FirFunctionDeclaration

                // Does the function accept a receiver ?
                if (resolvedFunction.receiver == null) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Function '${resolvedFunction.name}' does not accept a receiver",
                        astNode = astNode, span = loweredBase.fromAstNode.span
                    )
                )

                // Does the function accept a receiver with the good type ?

                // - is the type of the function "self" ?

                if (resolvedFunction.receiver!!.type is Type.Self) {

                } else if (!(resolvedFunction.receiver!!.type accepts loweredBase.base.type())) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Function '${resolvedFunction.name}' does not accept a receiver of type '${loweredBase.base.type()}'",
                        astNode = astNode, span = loweredBase.base.fromAstNode.span
                    )
                )

                // Set FirFunctionCall receiver
                firFunctionCall.receiver = loweredBase.base

                processFunctionCall(firFunctionCall, resolvedFunction, astNode)
            }
            else -> DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "lowered function call base expr resolved to '${loweredBase::class.simpleName}' but should have been FirResolvedReference or FirExpression.MemberAccess",
                    astNode = astNode
                )
            )
        }
    }

    private fun processFunctionCall(firCall: FirExpression.FunctionCall, functionSymbol: FirFunctionDeclaration, callAstNode: CyanFunctionCall): FirExpression.FunctionCall {
        // Set FirFunctionCall callee to the resolved function
        firCall.callee = functionSymbol.makeResolvedRef(firCall)

        // For each argument in the call, check if it matches with the function's arguments
        for ((index, astArg) in callAstNode.args.withIndex()) {
            // Check arg exists
            if (index !in functionSymbol.args.indices) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Too many arguments for function '${functionSymbol.name}'",
                    astNode = callAstNode, span = callAstNode.args[index].span
                )
            )

            val loweredArg = ExpressionLower.lower(astArg.value, firCall.args)
            val functionArg = functionSymbol.args[index]

            // Check arg value is accepted by type
            if (!(functionArg.typeAnnotation accepts loweredArg.type())) DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "Type mismatch: expected '${functionArg.typeAnnotation}', found '${loweredArg.type()}''",
                    astNode = callAstNode, span = astArg.span
                )
            )

            // Add to the FirFunctionCall
            firCall.args += loweredArg
        }

        // Check we have enough arguments
        if (firCall.args.size < functionSymbol.args.size) DiagnosticPipe.report (
            CompilerDiagnostic (
                level = CompilerDiagnostic.Level.Error,
                message = "Not enough arguments for function '${functionSymbol.name}'",
                astNode = callAstNode, span = callAstNode.span
            )
        )

        return firCall
    }

}
