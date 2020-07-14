package cyan.interpreter.evaluator.values

import cyan.interpreter.Interpreter
import cyan.interpreter.stack.StackFrame

interface CyanCallable : CyanValue<Any> {

    override val value get() = CyanStringValue("<callable>")

    fun call(interpreter: Interpreter, inStackFrame: StackFrame, callArgs: Array<CyanValue<out Any>>): CyanValue<out Any>

}
