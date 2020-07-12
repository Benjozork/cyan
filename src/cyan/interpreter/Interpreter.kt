package cyan.interpreter

import cyan.compiler.parser.items.*
import cyan.compiler.parser.items.expression.CyanIdentifierExpression
import cyan.compiler.parser.items.function.CyanFunctionCall
import cyan.compiler.parser.items.function.CyanFunctionDeclaration
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.stack.StackFrame

var indent = 0

fun iprintln(msg: Any?) = println("${"    ".repeat(indent)}| interpreter > $msg")
fun ioutput(msg: Any?) = println("${"    ".repeat(indent)}| >>> $msg")

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
            is CyanVariableDeclaration -> stackFrame.localVariables[statement.name.value] = statement.value
            is CyanFunctionCall -> {
                val (function, args) = statement
                when (function.value) {
                    "print" -> {
                        val arg = args[0]
                        ioutput(evaluate(if (arg is CyanIdentifierExpression) stackFrame.localVariables[arg.value]!! else arg, stackFrame))
                    }
                    in stackFrame.scopedFunctions -> {
                        val functionToExecute = stackFrame.scopedFunctions[statement.functionName.value]!!
                        val newStackFrame = StackFrame()
                        functionToExecute.signature.args.forEachIndexed { i, a ->
                            newStackFrame.localVariables[a.value] = statement.args.getOrNull(i)
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
