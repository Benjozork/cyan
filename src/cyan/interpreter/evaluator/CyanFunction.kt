package cyan.interpreter.evaluator

import cyan.compiler.parser.ast.CyanSource
import cyan.interpreter.Interpreter
import cyan.interpreter.stack.StackFrame

class CyanFunction(val name: String, val args: Array<String>, val source: CyanSource) : CyanCallable {

    override fun call(interpreter: Interpreter, inStackFrame: StackFrame, callArgs: Array<CyanValue<out Any>>): CyanValue<out Any> {
        val newStackFrame = StackFrame()
        callArgs.forEachIndexed { i, a -> newStackFrame.localVariables[args[i]] = a }

        interpreter.run(source, newStackFrame)

        return CyanStringValue("test")
    }

    override fun toString() = "$name(${args.joinToString(",")}) { ... }"

}
