package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.parser.ast.CyanSource

object SourceLower : Ast2FirLower<CyanSource, FirSource> {

    override fun lower(astNode: CyanSource, parentFirNode: FirNode): FirSource {
        val source = FirSource()

        for (node in astNode.statements) {
            val loweredNode = StatementLower.lower(node, source)

            if (loweredNode is FirStatement)
                source.statements += loweredNode
        }

        return source
    }


}
