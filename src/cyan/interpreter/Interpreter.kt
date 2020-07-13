package cyan.interpreter

import cyan.compiler.parser.ast.*
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.interpreter.evaluator.CyanCallable
import cyan.interpreter.evaluator.CyanFunction
import cyan.interpreter.evaluator.CyanValue
import cyan.interpreter.evaluator.evaluate
import cyan.interpreter.resolver.Resolver
import cyan.interpreter.stack.StackFrame

var indent = -1

fun iprintln(msg: Any?) = println("${"    ".repeat(indent)}| interpreter > $msg")
fun ierror(msg: Any?): Nothing {
    System.err.println("${"    ".repeat(indent)}| err: $msg")
    error("interpreter stopped because of error")
}
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
        stackFrame.printDebug()
        indent--
    }

    private fun executeStatement(statement: CyanStatement, stackFrame: StackFrame) {
        iprintln("executing ${statement::class.simpleName} - $statement")
        when (statement) {
            is CyanVariableDeclaration -> stackFrame.localVariables[statement.name.value] = evaluate(statement.value, stackFrame)
            is CyanFunctionDeclaration -> {
                val function = CyanFunction(statement.signature.name.value, statement.signature.args.map { it.value }.toTypedArray(), statement.source)

                stackFrame.scopedFunctions[function.name] = function
            }
            is CyanIfChain -> {
                val blocks = statement.ifStatements
                val goodBranch = blocks.firstOrNull { branch -> evaluate(branch.conditionExpr, stackFrame).value == true }

                goodBranch?.let { run(it.block, stackFrame) }
                    ?: statement.elseBlock?.let { run(it, stackFrame) }
            }
            is CyanFunctionCall -> {
                val (identifier, args) = statement
                when (identifier.value) {
                    "print" -> {
                        val value = evaluate(args[0], stackFrame)
                        ioutput(value)
                    }
                    "dbg" -> stackFrame.printDebug()
                    else -> {
                        val found = Resolver.findByIdentifier(identifier, stackFrame)
                        val arguments = args.map { evaluate(it, stackFrame) }.toTypedArray()

                        if (found is CyanCallable)
                            found.call(this, stackFrame, arguments)
                        else ierror("${identifier.value} is not a callable value")
                    }
                }
            }
            else -> error("can't evaluate statement of type ${statement::class.simpleName}")
        }
    }

}
