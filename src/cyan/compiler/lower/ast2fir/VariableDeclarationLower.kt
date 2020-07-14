package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.CyanVariableDeclaration

object VariableDeclarationLower : Ast2FirLower<CyanVariableDeclaration, FirVariableDeclaration> {

    override fun lower(astNode: CyanVariableDeclaration, parentFirNode: FirNode): FirVariableDeclaration {
        val firVariableDeclaration = FirVariableDeclaration(astNode.name.value, ExpressionLower.lower(astNode.value, parentFirNode))

        require(parentFirNode is FirScope) { "parentNode is not FirScope" }

        parentFirNode.declaredSymbols += firVariableDeclaration.name

        return firVariableDeclaration
    }

}
