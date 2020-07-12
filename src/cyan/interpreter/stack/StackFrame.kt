package cyan.interpreter.stack

import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.interpreter.evaluator.CyanValue

class StackFrame {

    val localVariables = mutableMapOf<String, CyanValue<out Any>>()

    val scopedFunctions = mutableMapOf<String, CyanFunctionDeclaration>()

}
