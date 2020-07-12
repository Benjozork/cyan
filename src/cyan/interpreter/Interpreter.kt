package cyan.interpreter

import cyan.compiler.parser.items.CyanFunctionCall
import cyan.compiler.parser.items.CyanSource
import cyan.compiler.parser.items.CyanStatement
import cyan.compiler.parser.items.CyanVariableDeclaration
import cyan.compiler.parser.items.expression.literal.CyanReferenceExpression
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.stack.StackFrame

fun iprintln(msg: Any?) = println("interpreter > $msg")

class Interpreter {

    fun run(source: CyanSource) {
        val stackFrame = StackFrame()

        println("--- stack frame initialized, executing source ---")

        for (statement in source.statements) {
            executeStatement(statement, stackFrame)
        }

        println("--- execution finished ---")
        println("stk { lv: ${stackFrame.localVariables} }")
    }

    private fun executeStatement(statement: CyanStatement, stackFrame: StackFrame) {
        iprintln("executing ${statement::class.simpleName} - $statement")
        when (statement) {
            is CyanVariableDeclaration -> stackFrame.localVariables[statement.name.value] = statement.value
            is CyanFunctionCall        -> {
                val (function, args) = statement
                when (function.value) {
                    "print" -> {
                        val arg = args[0]
                        println(evaluate(if (arg is CyanReferenceExpression) stackFrame.localVariables[arg.value]!! else arg, stackFrame))
                    }
                }
            }
        }
    }

}
