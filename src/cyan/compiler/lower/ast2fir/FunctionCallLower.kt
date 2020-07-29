package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.functions.FirFunctionCall
import cyan.compiler.lower.ast2fir.expression.ExpressionLower
import cyan.compiler.parser.ast.function.CyanFunctionCall

object FunctionCallLower : Ast2FirLower<CyanFunctionCall, FirFunctionCall> {

    override fun lower(astNode: CyanFunctionCall, parentFirNode: FirNode): FirFunctionCall {
        val functionNameReference = FirReference(parentFirNode, astNode.functionIdentifier.value)
        val resolvedFunctionSymbol = parentFirNode.findSymbol(functionNameReference)
            ?: error("ast2fir: cannot find symbol: ${astNode.functionIdentifier.value}")

        val firFunctionCall = FirFunctionCall(parentFirNode, resolvedFunctionSymbol, emptyArray())

        val functionDeclarationArgsToPassedArgs = ((resolvedFunctionSymbol as FirFunctionDeclaration).args zip astNode.args).toMap()
                .mapValues { (_, astArg) -> ExpressionLower.lower(astArg, firFunctionCall) }

        functionDeclarationArgsToPassedArgs.entries.forEachIndexed { i, (firArg, astArg) -> // Type check args
            val astArgType = astArg.type()

            if (!(firArg.typeAnnotation accepts astArgType)) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Type mismatch for argument $i: expected '${firArg.typeAnnotation}', found '${astArgType}'",
                        astNode = astNode,
                        span = astArg.astExpr.span
                    )
                )
            }
        }

        return firFunctionCall.also { it.args = functionDeclarationArgsToPassedArgs.values.toTypedArray() }
    }

}
