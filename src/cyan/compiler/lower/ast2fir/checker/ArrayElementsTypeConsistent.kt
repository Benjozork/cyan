package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.indent
import cyan.interpreter.stack.StackFrame

object ArrayElementsTypeConsistent : Check<FirExpression> {

    override fun check(firNode: FirExpression, containingNode: FirNode): Boolean {
        if (firNode.astExpr !is CyanArrayExpression) return false

        val stackFrame = StackFrame()

        val containingScope = firNode.firstAncestorOfType<FirScope>()
        val containingScopeLvs = containingScope?.declaredSymbols?.filterIsInstance<FirVariableDeclaration>()?.map {
            val lvStackFrame = StackFrame()
            indent++
            val evaluated = evaluate(it.initializationExpr.astExpr, lvStackFrame)
            indent--

            it.name to evaluated
        }?.toMap() ?: emptyMap()

        stackFrame.localVariables.putAll(containingScopeLvs)

        indent++
        val arrayValueTypeSet = firNode.astExpr.exprs.map { evaluate(it, stackFrame)::class }.toSet()
        indent--

        return arrayValueTypeSet.size > 1
    }


}
