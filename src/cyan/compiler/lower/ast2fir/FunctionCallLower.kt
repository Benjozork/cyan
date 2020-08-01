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
        val firFunctionCall = FirFunctionCall(parentFirNode)

        val functionNameReference = FirReference(firFunctionCall, astNode.functionIdentifier.value, astNode.functionIdentifier)
        val resolvedFunctionReference = firFunctionCall.findSymbol(functionNameReference)
            ?: error("ast2fir: cannot find symbol: ${astNode.functionIdentifier.value}")

        firFunctionCall.callee = resolvedFunctionReference

        val functionDeclarationArgsToPassedArgs = ((resolvedFunctionReference.resolvedSymbol as FirFunctionDeclaration).args zip astNode.args).toMap()
                .mapValues { (_, astArg) -> ExpressionLower.lower(astArg, firFunctionCall) }

        functionDeclarationArgsToPassedArgs.entries.forEachIndexed { i, (firArg, astArg) -> // Type check args
            val astArgType = astArg.type()

            if (!(firArg.typeAnnotation accepts astArgType)) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Type mismatch for argument $i: expected '${firArg.typeAnnotation}', found '${astArgType}'",
                        astNode = astNode,
                        span = astArg.fromAstNode.span
                    )
                )
            }
        }

        firFunctionCall.args = functionDeclarationArgsToPassedArgs.values.toTypedArray()

        return firFunctionCall.also { it.args = functionDeclarationArgsToPassedArgs.values.toTypedArray() }
    }

}
