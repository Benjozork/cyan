package cyan.compiler.fir.expression

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.common.types.CyanType
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

class FirExpression(override val parent: FirNode, val astExpr: CyanExpression) : FirNode {

    /**
     * Infers the type of this FirExpression node.
     */
    fun type(): Type {
         return when (astExpr) {
             is CyanNumericLiteralExpression -> Type.Primitive(CyanType.Int32, false)
             is CyanStringLiteralExpression  -> Type.Primitive(CyanType.Str, false)
             is CyanBooleanLiteralExpression -> Type.Primitive(CyanType.Bool, false)
             is CyanArrayExpression          -> FirExpression(this, astExpr.exprs.first()).type().asArrayType()
             is CyanBinaryExpression -> {
                 val (lhsType, rhsType) = FirExpression(this, astExpr.lhs).type() to FirExpression(this, astExpr.rhs).type()

                 if (lhsType != rhsType) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "binary expressions with different operand types are not yet supported",
                             astNode = astExpr
                         )
                     )
                 }

                 lhsType
             }
             is CyanIdentifierExpression -> {
                 val containingScope = this.containingScope()

                 when (val referee = containingScope?.findSymbol(FirReference(this, this.astExpr.value))) {
                     is FirVariableDeclaration -> referee.initializationExpr.type()
                     is FirFunctionDeclaration -> Type.Primitive(CyanType.Any, false)
                     is FirFunctionArgument -> referee.typeAnnotation
                     null -> error("cannot find symbol '${astExpr.value}'")
                     else -> error("can't infer type of ${referee::class.simpleName}")
                 }
             }
             is CyanMemberAccessExpression -> Type.Primitive(CyanType.Any, false)
             is CyanArrayIndexExpression -> {
                 return FirExpression(this, astExpr.base).type().also {
                     if (!it.array) {
                         DiagnosticPipe.report (
                             CompilerDiagnostic (
                                 level = CompilerDiagnostic.Level.Error,
                                 message = "left-hand side of array index expression must refer to an array",
                                 astNode = astExpr
                             )
                         )
                     }
                 }
             }
             else -> error("can't infer type of ${astExpr::class.simpleName}")
        }
    }

    override fun allReferredSymbols(): Set<FirSymbol> = emptySet()

}
