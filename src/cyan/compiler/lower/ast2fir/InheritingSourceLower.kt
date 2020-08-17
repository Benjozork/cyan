package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirStatement
import cyan.compiler.fir.FirTypeDeclaration
import cyan.compiler.fir.FirSource
import cyan.compiler.parser.ast.CyanSource

object InheritingSourceLower : Ast2FirLower<CyanSource, FirSource> {

    override fun lower(astNode: CyanSource, parentFirNode: FirNode): FirSource {
        val source = FirSource(parentFirNode, isInheriting = true)

        for (node in astNode.statements) {
            val loweredNode = StatementLower.lower(node, source)

            if (loweredNode is FirStatement && loweredNode !is FirTypeDeclaration)
                source.statements.add(loweredNode)
        }

        return source
    }


}
