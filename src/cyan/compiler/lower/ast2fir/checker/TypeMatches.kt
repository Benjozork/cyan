package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirVariableDeclaration

object TypeMatches : Check<FirVariableDeclaration> {

    override fun check(firNode: FirVariableDeclaration, containingNode: FirNode): Boolean {
        val typeAnnotationType = firNode.typeAnnotation
        val valueEffectiveType = firNode.initializationExpr.type()

        return when {
            typeAnnotationType == null -> false
            !(typeAnnotationType accepts valueEffectiveType) -> true
            else -> false
        }
    }

}
