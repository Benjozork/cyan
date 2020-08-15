package cyan.compiler.lower.ast2fir.expression

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.firstAncestorOfType
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

                resolvedReference
            }
            is CyanStructLiteralExpression -> {
                val containingVariableDeclaration = parentFirNode.firstAncestorOfType<FirVariableDeclaration>() ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "struct literals are only allowed in variable declarations",
                        astNode = astNode
                    )
                )

                val type = if (astNode.typeAnnotation != null) {
                    val reference = FirReference(parentFirNode, astNode.typeAnnotation.identifierExpression.value, astNode)
                    val typeSymbol = parentFirNode.findSymbol(reference) ?: DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "Unresolved type '${astNode.typeAnnotation.identifierExpression.value}'",
                            astNode = astNode, span = astNode.typeAnnotation.span
                        )
                    )

                    (typeSymbol.resolvedSymbol as FirTypeDeclaration).struct
                } else {
                    containingVariableDeclaration.typeAnnotation ?: DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "cannot infer type for struct literal",
                            astNode = astNode
                        )
                    )
                }

                if (type !is Type.Struct) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Type mismatch: expected '$type', found <struct literal>",
                        astNode = astNode
                    )
                )

                if (type.properties.size < astNode.exprs.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Not enough arguments for type $type",
                        astNode = astNode
                    )
                ) else if (type.properties.size > astNode.exprs.size) DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Too many arguments for type $type",
                        astNode = astNode
                    )
                )

                val fieldValues = type.properties zip astNode.exprs.map { lower(it, parentFirNode) }

                fieldValues.forEachIndexed { i, (property, astExpr) ->
                    if (!(property.type accepts astExpr.type())) DiagnosticPipe.report (
                        CompilerDiagnostic (
                            level = CompilerDiagnostic.Level.Error,
                            message = "Type mismatch for argument #$i (field '${property.name}'): expected '${property.type}', found '${astExpr.type()}'",
                            astNode = astNode
                        )
                    )
                }

                val firStructLiteral = FirExpression.Literal.Struct(fieldValues.toMap(), type, parentFirNode, astNode)

                firStructLiteral
            }
            is CyanArrayExpression -> {
                val loweredElements = astNode.exprs.map { lower(it, parentFirNode) }
                val firArray = FirExpression.Literal.Array(loweredElements, parentFirNode, astNode)

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
