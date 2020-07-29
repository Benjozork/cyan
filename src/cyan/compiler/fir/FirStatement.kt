package cyan.compiler.fir

import cyan.compiler.fir.extensions.firstAncestorOfType

interface FirStatement : FirNode {

    fun delete() = firstAncestorOfType<FirSource>()?.handleDeletionOfChild(this)

    fun replaceWith(otherStatements: List<FirStatement>) = firstAncestorOfType<FirSource>()?.handleReplacementOfChild(this, otherStatements)

}
