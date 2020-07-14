package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirFunctionDeclaration
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirNullNode
import cyan.compiler.fir.FirScope
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object FunctionDeclarationLower : Ast2FirLower<CyanFunctionDeclaration, FirNullNode> {

    override fun lower(astNode: CyanFunctionDeclaration, parentFirNode: FirNode): FirNullNode {
        // TODO find a way to pass actual FirFunctionDeclaration node to SourceLower
        val firFunctionDeclaration = FirFunctionDeclaration(astNode.signature.name.value, SourceLower.lower(astNode.source, parentFirNode))

        require (parentFirNode is FirScope) { "ast2fir: parentFirNode must be FirSource but was ${parentFirNode::class.simpleName}" }

        parentFirNode.localFunctions.add(firFunctionDeclaration)

        return FirNullNode
    }

}
