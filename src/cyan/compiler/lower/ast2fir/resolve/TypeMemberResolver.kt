package cyan.compiler.lower.ast2fir.resolve

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirSymbol

object TypeMemberResolver {

    fun resolve(type: Type.Struct, memberReference: FirReference): FirResolvedReference? {
        val tryWith = listOf(::findInTraitDerives)

        for (function in tryWith) {
            function(type, memberReference)?.let { return it }
        }

        return null
    }

    private fun findInTraitDerives(type: Type.Struct, memberReference: FirReference): FirResolvedReference? {
        val typeSelfDerives = type.derives
        val typeSelfDerivesImpls = typeSelfDerives.flatMap { ( it.functionImpls + it.propertyImpls).toList() }.toMap()

        return typeSelfDerives.flatMap { it.trait.elements.toList() }.firstOrNull { it.name == memberReference.text }?.let {
            typeSelfDerivesImpls[it]?.let { node ->
                when (node) {
                    is FirSymbol -> node.makeResolvedRef(memberReference.parent)
                    else -> error("cannot make a resolved reference to a trait impl of type '${node::class.simpleName}'")
                }
            }
        }
    }

}
