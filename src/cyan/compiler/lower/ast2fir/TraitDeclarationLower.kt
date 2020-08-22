package cyan.compiler.lower.ast2fir

import cyan.compiler.common.types.Type
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.fir.extensions.resolveType
import cyan.compiler.parser.ast.types.CyanTraitDeclaration

object TraitDeclarationLower : Ast2FirLower<CyanTraitDeclaration, FirTypeDeclaration.Trait> {

    override fun lower(astNode: CyanTraitDeclaration, parentFirNode: FirNode): FirTypeDeclaration.Trait {
        val decl = FirTypeDeclaration.Trait(parentFirNode)

        val loweredElements = astNode.elements.map {
            when (it) {
                is CyanTraitDeclaration.Element.Function -> {
                    val loweredFunctionDeclaration = FunctionDeclarationLower.lower(it.functionDeclaration, decl)

                    Type.Trait.Element.Function(loweredFunctionDeclaration.name, loweredFunctionDeclaration.args, loweredFunctionDeclaration.returnType)
                }
                is CyanTraitDeclaration.Element.Property -> {
                    val resolvedType = decl.resolveType(it.type)

                    Type.Trait.Element.Property(it.name.value, resolvedType)
                }
            }
        }.toTypedArray()

        val traitType = Type.Trait(astNode.name.value, loweredElements, false)

        decl.type = traitType

        (parentFirNode as FirScope).declaredSymbols += decl

        return decl
    }

}
