package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.types.CyanStructDeclaration

object StatementLower : Ast2FirLower<CyanStatement, FirNode> {

    override fun lower(astNode: CyanStatement, parentFirNode: FirNode): FirNode {
        return when (astNode) {
            is CyanImportStatement     -> ImportStatementLower.lower(astNode, parentFirNode)
            is CyanVariableDeclaration -> VariableDeclarationLower.lower(astNode, parentFirNode)
            is CyanFunctionDeclaration -> FunctionDeclarationLower.lower(astNode, parentFirNode)
            is CyanStructDeclaration   -> StructDeclarationLower.lower(astNode, parentFirNode)
            is CyanFunctionCall        -> FunctionCallLower.lower(astNode, parentFirNode)
            is CyanIfChain             -> IfChainLower.lower(astNode, parentFirNode)
            is CyanWhileStatement      -> WhileStatementLower.lower(astNode, parentFirNode)
            is CyanForStatement        -> ForStatementLower.lower(astNode, parentFirNode)
            is CyanAssignment          -> AssignLower.lower(astNode, parentFirNode)
            is CyanReturn              -> ReturnLower.lower(astNode, parentFirNode)
            else -> error("ast2ir: cannot lower AST statement of type ${astNode::class.simpleName}")
        }
    }

}
