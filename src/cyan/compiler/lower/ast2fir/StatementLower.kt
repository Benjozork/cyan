package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.parser.ast.CyanIfChain
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.CyanVariableDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object StatementLower : Ast2FirLower<CyanStatement, FirNode> {

    override fun lower(astNode: CyanStatement, parentFirNode: FirNode): FirNode {
        return when (astNode) {
            is CyanVariableDeclaration -> VariableDeclarationLower.lower(astNode, parentFirNode)
            is CyanFunctionDeclaration -> FunctionDeclarationLower.lower(astNode, parentFirNode)
            is CyanFunctionCall        -> FunctionCallLower.lower(astNode, parentFirNode)
            is CyanIfChain             -> IfChainLower.lower(astNode, parentFirNode)
            else -> error("ast2ir: cannot lower AST statement of type ${astNode::class.simpleName}")
        }
    }

}
