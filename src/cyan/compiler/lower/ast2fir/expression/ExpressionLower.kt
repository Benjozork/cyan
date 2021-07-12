package cyan.compiler.lower.ast2fir.expression

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.Ast2FirLower
import cyan.compiler.lower.ast2fir.FunctionCallLower
import cyan.compiler.lower.ast2fir.expression.string.StringContentParser
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

object ExpressionLower : Ast2FirLower<CyanExpression, FirExpression> {

    private val stringContentParser = StringContentParser()

    override fun lower(astNode: CyanExpression, parentFirNode: FirNode): FirExpression {
        return when (astNode) {
            is CyanNumericLiteralExpression -> FirExpression.Literal.Number(astNode.value, parentFirNode, astNode)
            is CyanStringLiteralExpression -> {
                val content = stringContentParser.parseToEnd(astNode.value).toString()

                FirExpression.Literal.String(content, parentFirNode, astNode)
            }
            is CyanBooleanLiteralExpression -> FirExpression.Literal.Boolean(astNode.value, parentFirNode, astNode)
            is CyanFunctionCall -> when (val node = FunctionCallLower.lower(astNode, parentFirNode)) {
                is FirExpression.FunctionCall   -> node
                is FirExpression.Literal.Struct -> node
                else -> DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Internal,
                        message = "CyanFunctionCall was lowered into something unexpected: ${node::class.simpleName}",
                        astNode = astNode, span = astNode.span
                    )
                )
            }
            is CyanIdentifierExpression -> {
                val reference = FirReference(parentFirNode, astNode.value, astNode)
                val resolvedReference = parentFirNode.findSymbol(reference) ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Unresolved symbol '${astNode.value}'",
                        astNode = astNode
                    )
                )

                if (parentFirNode !is FirExpression.FunctionCall && resolvedReference.resolvedSymbol is FirReflectedElement) {
                    val makeFunctionBoxRef = FirReference(parentFirNode, "make_function_box", CyanIdentifierExpression("make_empty_function_box"))
                    val makeFunctionBox = parentFirNode.findSymbol(makeFunctionBoxRef)

                    makeFunctionBox ?: DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Internal,
                            astNode = astNode,
                            message = "could not find 'make_function_box' function",
                            span = astNode.span
                        )
                    )

                    val firCall = FirExpression.FunctionCall(parentFirNode, CyanFunctionCall(astNode, emptyArray()))
                    firCall.callee = makeFunctionBox
                    firCall.args += listOf (
                        FirExpression.Literal.String(resolvedReference.resolvedSymbol.name, firCall, CyanStringLiteralExpression(resolvedReference.resolvedSymbol.name)),
                        FirExpression.Literal.Array (
                            elements = (resolvedReference.resolvedSymbol as FirFunctionDeclaration).attributes
                                .map { FirExpression.Literal.String(it.ident.text, firCall, CyanStringLiteralExpression(it.ident.text)) },
                            parent = firCall,
                            fromAstNode = CyanArrayExpression(resolvedReference.resolvedSymbol.attributes.map { CyanStringLiteralExpression(it.ident.text) }.toTypedArray())
                        ),
                        FirExpression.Literal.Array (
                            elements = resolvedReference.resolvedSymbol.args
                                .map { FirExpression.Literal.String(it.name, firCall, CyanStringLiteralExpression(it.name)) },
                            parent = firCall,
                            fromAstNode = CyanArrayExpression(resolvedReference.resolvedSymbol.args.map { CyanStringLiteralExpression(it.name) }.toTypedArray())
                        )
                    )

                    return firCall
                }

                resolvedReference
            }
            is CyanArrayExpression -> {
                val loweredElements = astNode.exprs.map { lower(it, parentFirNode) }
                val firArray = FirExpression.Literal.Array(loweredElements, parentFirNode, astNode)

                if (loweredElements.isEmpty() && (parentFirNode is FirVariableDeclaration && parentFirNode.typeAnnotation == null)) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Empty arrays can only be assigned to variables with explicitly specified types",
                        astNode = astNode
                    )
                )

                loweredElements.map(FirExpression::type).toSet().takeIf { it.size <= 1 } ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Arrays can only contain elements of the same type",
                        astNode = astNode
                    )
                )

                loweredElements.forEach { it.parent = firArray }

                firArray
            }
            is CyanBinaryExpression -> {
                val firLhs = lower(astNode.lhs, parentFirNode)
                val firRhs = lower(astNode.rhs, parentFirNode)

                val firBinaryExpression = FirExpression.Binary(firLhs, astNode.operator, firRhs, parentFirNode, astNode)

                firLhs.parent = firBinaryExpression
                firRhs.parent = firBinaryExpression

                firBinaryExpression
            }
            is CyanMemberAccessExpression -> {
                val base = lower(astNode.base, parentFirNode)

                val firMemberAccess = FirExpression.MemberAccess(base, astNode.member.value, parentFirNode, astNode)
                base.parent = firMemberAccess

                firMemberAccess
            }
            is CyanArrayIndexExpression -> {
                val base = lower(astNode.base, parentFirNode)

                val firArrayIndex = FirExpression.ArrayIndex(base, lower(astNode.index, parentFirNode), parentFirNode, astNode)

                firArrayIndex.base.parent = firArrayIndex
                firArrayIndex.index.parent = firArrayIndex

                firArrayIndex
            }
            else -> error("ast2fir: cannot lower expression of type ${astNode::class.simpleName}")
        }

//        if (firExpression.astExpr is CyanArrayExpression) {
//            if (ArrayElementsTypeConsistent.check(firExpression, parentFirNode)) {
//                DiagnosticPipe.report (
//                    CompilerDiagnostic (
//                        level = CompilerDiagnostic.Level.Error,
//                        message = "array elements must all be of the same type",
//                        astNode = astNode
//                    )
//                )
//            }
//        }
    }

}
