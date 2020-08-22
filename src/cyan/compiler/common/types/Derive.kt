package cyan.compiler.common.types

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionDeclaration

class Derive (
    override var parent: FirNode,
    val trait: Type.Trait,
) : FirNode {

    lateinit var functionImpls: Map<Type.Trait.Element.Function, FirFunctionDeclaration>
    lateinit var propertyImpls: Map<Type.Trait.Element.Property, FirExpression>

    lateinit var onType: Type

    override fun allReferredSymbols() = (functionImpls.flatMap { it.value.allReferredSymbols() } + propertyImpls.flatMap { it.value.allReferredSymbols() }).toSet()

}
