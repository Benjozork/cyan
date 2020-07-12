package cyan.interpreter

import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.interpreter.evaluator.CyanValue
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.stack.StackFrame

var indent = -1

fun iprintln(msg: Any?) = println("${"    ".repeat(indent)}| interpreter > $msg")
fun ioutput(msg: CyanValue<out Any>) = println("${"    ".repeat(indent)}| >>> $msg")

class Interpreter {

    fun run(source: CyanSource, initialStackFrame: StackFrame? = null) {
        val stackFrame = initialStackFrame ?: StackFrame()

        indent++
        println("${"    ".repeat(indent)}--- stack frame initialized, executing source ---")

        for (statement in source.statements) {
            executeStatement(statement, stackFrame)
        }

        println("${"    ".repeat(indent)}--- execution finished ---")
        println("${"    ".repeat(indent)}stk { lv: ${stackFrame.localVariables} }")
        indent--
    }

    private fun executeStatement(statement: CyanStatement, stackFrame: StackFrame) {
        iprintln("executing ${statement::class.simpleName} - $statement")
        when (statement) {
            is CyanVariableDeclaration -> stackFrame.localVariables[statement.name.value] = evaluate(statement.value!!, stackFrame)
            is CyanFunctionCall -> {
                val (function, args) = statement
                when (function.value) {
                    "print" -> {
                        val value = evaluate(args[0], stackFrame)
                        ioutput(value)
                    }
                    in stackFrame.scopedFunctions -> {
                        val functionToExecute = stackFrame.scopedFunctions[statement.functionName.value]!!
                        val newStackFrame = StackFrame()
                        functionToExecute.signature.args.forEachIndexed { i, a ->
                            newStackFrame.localVariables[a.value] = evaluate(statement.args.getOrNull(i)!!, stackFrame)
                        }

                        this.run(functionToExecute.source, newStackFrame)
                    }
                }
            }
            is CyanFunctionDeclaration -> stackFrame.scopedFunctions[statement.signature.name.value] = statement
            else -> error("can't evaluate statement of type ${statement::class.simpleName}")
        }
    }

}
