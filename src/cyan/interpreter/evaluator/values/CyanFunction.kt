package cyan.interpreter.evaluator.values

import cyan.compiler.parser.ast.CyanSource
import cyan.interpreter.Interpreter
import cyan.interpreter.ierror
import cyan.interpreter.stack.StackFrame

class CyanFunction(val name: String, val args: Array<String>, val source: CyanSource) : CyanCallable {

    override fun call(interpreter: Interpreter, inStackFrame: StackFrame, callArgs: Array<CyanValue<out Any>>): CyanValue<out Any> {
        if (callArgs.size < this.args.size) {
            ierror("not enough arguments for function $this")
        } else if (callArgs.size > this.args.size) {
            ierror("too many arguments for function $this")
        }

        val newStackFrame = StackFrame()
        callArgs.forEachIndexed { i, a -> newStackFrame.localVariables[args[i]] = a }

        interpreter.run(source, newStackFrame)

        return CyanStringValue("test")
    }

    override fun toString() = "$name(${args.joinToString(", ")}) { ... }"

}
