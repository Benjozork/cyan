package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirFunctionCall
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.function.CyanFunctionCall

object FunctionCallLower : Ast2FirLower<CyanFunctionCall, FirFunctionCall> {

    override fun lower(astNode: CyanFunctionCall, parentFirNode: FirNode): FirFunctionCall {
        val functionNameReference = FirReference(parentFirNode, astNode.functionIdentifier.value)
        val resolvedFunctionSymbol = parentFirNode.findSymbol(functionNameReference)
            ?: error("ast2fir: cannot find symbol: ${astNode.functionIdentifier.value}")

        return FirFunctionCall (
            parent = parentFirNode,
            callee = resolvedFunctionSymbol,
            args = astNode.args.map { ExpressionLower.lower(it, parentFirNode) }.toTypedArray()
        )
    }

}
