package cyan.compiler.fir.expression

import cyan.compiler.fir.*
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.parser.ast.CyanType
import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanBinaryExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

class FirExpression(override val parent: FirNode, val astExpr: CyanExpression) : FirNode {

    /**
     * Infers the type of this FirExpression node.
     */
    fun type(): FirTypeAnnotation {
         return when (astExpr) {
             is CyanNumericLiteralExpression -> FirTypeAnnotation(CyanType.Int32, false)
             is CyanStringLiteralExpression  -> FirTypeAnnotation(CyanType.Str, false)
             is CyanBooleanLiteralExpression -> FirTypeAnnotation(CyanType.Bool, false)
             is CyanArrayExpression          -> FirExpression(this, astExpr.exprs.first()).type().copy(array = true)
             is CyanBinaryExpression -> {
                 val (lhsType, rhsType) = FirExpression(this, astExpr.lhs).type() to FirExpression(this, astExpr.rhs).type()

                 require (lhsType == rhsType) { "binary expressions with different operand types are not yet supported" }

                 lhsType
             }
             is CyanIdentifierExpression -> {
                val containingScope = this.firstAncestorOfType<FirScope>()

                when (val referee = containingScope?.findSymbol(FirReference(this, this.astExpr.value))) {
                    is FirVariableDeclaration -> referee.initializationExpr.type()
                    null -> error("cannot find symbol '${astExpr.value}'")
                    else -> error("can't infer type of ${referee::class.simpleName}")
                }
            }
            else -> error("can't infer type of ${astExpr::class.simpleName}")
        }
    }

    override fun allReferredSymbols(): Set<FirSymbol> = emptySet()

}
