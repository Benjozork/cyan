package cyan.compiler.lower.ast2fir.checker

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.parser.ast.CyanType
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.evaluator.values.CyanArrayValue
import cyan.interpreter.evaluator.values.CyanBooleanValue
import cyan.interpreter.evaluator.values.CyanNumberValue
import cyan.interpreter.evaluator.values.CyanStringValue
import cyan.interpreter.indent
import cyan.interpreter.stack.StackFrame

object TypeMatches : Check<FirVariableDeclaration> {

    override fun check(firNode: FirVariableDeclaration, containingNode: FirNode): Boolean {
        val typeAnnotationType = when (firNode.typeAnnotation?.base) {
            CyanType.Int8,
            CyanType.Int32,
            CyanType.Int64,
            CyanType.Float32,
            CyanType.Float64 -> CyanNumberValue::class
            CyanType.Bool    -> CyanBooleanValue::class
            CyanType.Str     -> CyanStringValue::class
            CyanType.Char    -> CyanStringValue::class
            null -> return false
        }

        indent++
        val valueEffectiveType = evaluate(firNode.initializationExpr.astExpr, StackFrame())::class
        indent--

        if (valueEffectiveType == CyanArrayValue::class)
            return false

        return typeAnnotationType != valueEffectiveType
    }

}
