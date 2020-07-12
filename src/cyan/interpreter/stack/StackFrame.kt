package cyan.interpreter.stack

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.interpreter.evaluator.CyanCallable
import cyan.interpreter.evaluator.CyanStringValue
import cyan.interpreter.evaluator.CyanValue
import cyan.interpreter.ioutput
import cyan.interpreter.resolver.ResolveScope

class StackFrame : ResolveScope {

    val localVariables = mutableMapOf<String, CyanValue<out Any>>()

    val scopedFunctions = mutableMapOf<String, CyanCallable>()

    fun printDebug() {
        ioutput(CyanStringValue("stk { lv: $localVariables, sf: $scopedFunctions }"))
    }

    override fun findByIdentifier(ident: CyanIdentifierExpression) = localVariables[ident.value] ?: scopedFunctions[ident.value]

}
