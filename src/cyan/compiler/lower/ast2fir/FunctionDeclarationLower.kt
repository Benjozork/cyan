package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirFunctionDeclaration
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

object FunctionDeclarationLower : Ast2FirLower<CyanFunctionDeclaration, FirFunctionDeclaration> {

    override fun lower(astNode: CyanFunctionDeclaration): FirFunctionDeclaration {
        return FirFunctionDeclaration(astNode.signature.name.value)
    }

}
