package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.functions.FirFunctionCall
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.function.CyanFunctionCall

object FunctionCallLower : Ast2FirLower<CyanFunctionCall, FirNode> {

    override fun lower(astNode: CyanFunctionCall, parentFirNode: FirNode): FirNode {
        val firFunctionCall = FirFunctionCall(parentFirNode)

        // Find what we are calling
        val resolvedFunctionReference = when (val loweredBase = ExpressionLower.lower(astNode.base, firFunctionCall)) {
            is FirResolvedReference -> loweredBase
            is FirExpression.MemberAccess -> when (val memberAccessBaseType = loweredBase.base.type()) {
                is Type.Primitive -> DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Internal,
                        message = "Primitives do not have functions",
                        astNode = astNode
                    )
                )
                is Type.Struct -> {
                    val structResolvedReference = parentFirNode.findSymbol(FirReference(parentFirNode, memberAccessBaseType.name, astNode))!!
                    val structDeclarationScope = structResolvedReference.resolvedSymbol.containingScope()!!
                    val matchingStructMethod = structDeclarationScope.localFunctions
                        .singleOrNull { it.args.first().typeAnnotation == memberAccessBaseType && it.name == loweredBase.member }
                        ?: DiagnosticPipe.report (
                            CompilerDiagnostic (
                                level = CompilerDiagnostic.Level.Error,
                                message = "Could not find a function called '${loweredBase.member}' that accepts '$memberAccessBaseType' as a receiver",
                                astNode = astNode, span = astNode.base.span
                            )
                        )

                    firFunctionCall.args += loweredBase.base

                    FirResolvedReference(loweredBase, matchingStructMethod, matchingStructMethod.name, loweredBase.fromAstNode)
                }
            }
            else -> DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Internal,
                    message = "lowered function call base expr resolved to '${loweredBase::class.simpleName}' but should have been FirResolvedReference",
                    astNode = astNode
                )
            )
        }

        // Check what we are calling
        return when (val symbol = resolvedFunctionReference.resolvedSymbol) {
            is FirTypeDeclaration -> {
                val type = symbol.struct

                if (type.properties.size < astNode.args.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Not enough arguments for type $type",
                        astNode = astNode
                    )
                ) else if (type.properties.size > astNode.args.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Too many arguments for type $type",
                        astNode = astNode
                    )
                )

                val fieldValues = type.properties zip astNode.args.map { ExpressionLower.lower(it, parentFirNode) }

                fieldValues.forEachIndexed { i, (property, astExpr) ->
                    if (!(property.type accepts astExpr.type())) DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "Type mismatch for argument #$i (field '${property.name}'): expected '${property.type}', found '${astExpr.type()}'",
                            astNode = astNode
                        )
                    )
                }

                FirExpression.Literal.Struct(fieldValues.toMap(), type, parentFirNode, astNode)
            }
            is FirFunctionDeclaration -> {
                val resolvedFunction = resolvedFunctionReference.resolvedSymbol as FirFunctionDeclaration

                firFunctionCall.callee = resolvedFunctionReference

                val functionDeclarationArgsToPassedArgs = ((resolvedFunctionReference.resolvedSymbol as FirFunctionDeclaration).args zip astNode.args).toMap()
                        .mapValues { (_, astArg) -> ExpressionLower.lower(astArg, firFunctionCall) }

                if (astNode.args.size < resolvedFunction.args.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Not enough arguments for function ${resolvedFunction.name}",
                        astNode = astNode
                    )
                ) else if (astNode.args.size > resolvedFunction.args.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Too many arguments for type ${resolvedFunction.name}",
                        astNode = astNode
                    )
                )

                functionDeclarationArgsToPassedArgs.entries.forEachIndexed { i, (firArg, astArg) -> // Type check args
                    val astArgType = astArg.type()

                    if (!(firArg.typeAnnotation accepts astArgType)) {
                        DiagnosticPipe.report (
                            CompilerDiagnostic (
                                level = CompilerDiagnostic.Level.Error,
                                message = "Type mismatch for argument $i: expected '${firArg.typeAnnotation}', found '${astArgType}'",
                                astNode = astNode,
                                span = astArg.fromAstNode.span
                            )
                        )
                    }
                }

                firFunctionCall.args += functionDeclarationArgsToPassedArgs.values.toTypedArray()

                FirExpression.FunctionCall(firFunctionCall, parentFirNode, astNode)
            }
            else -> error("ast2fir: calls to symbols of type '${symbol::class.simpleName}' are not supported yet")
        }
    }

}
