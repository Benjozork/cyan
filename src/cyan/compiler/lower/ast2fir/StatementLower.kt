package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirStatement
import cyan.compiler.parser.ast.CyanIfChain
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.CyanVariableDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object StatementLower : Ast2FirLower<CyanStatement, FirStatement> {

    override fun lower(astNode: CyanStatement): FirStatement {
        return when (astNode) {
            is CyanVariableDeclaration -> VariableDeclarationLower.lower(astNode)
            is CyanFunctionDeclaration -> FunctionDeclarationLower.lower(astNode)
            is CyanFunctionCall -> FunctionCallLower.lower(astNode)
            is CyanIfChain -> IfChainLower.lower(astNode)
            else -> error("ast2ir: cannot lower AST statement of type ${astNode::class.simpleName}")
        }
    }

}
