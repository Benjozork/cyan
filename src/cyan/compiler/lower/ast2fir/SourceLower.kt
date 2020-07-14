package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirStatement
import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.parser.ast.CyanStatement

object SourceLower : Ast2FirLower<CyanSource, FirSource> {

    override fun lower(astNode: CyanSource): FirSource {
        val loweredNodes = mutableListOf<FirStatement>()

        for (node in astNode.statements) {
            loweredNodes += when (node) {
                is CyanStatement -> StatementLower.lower(node)
                else -> error("cannot lower AST node of type ${node::class.simpleName}")
            }
        }

        return FirSource(loweredNodes.toTypedArray())
    }


}
