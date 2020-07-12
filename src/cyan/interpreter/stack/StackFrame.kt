package cyan.interpreter.stack

import cyan.compiler.parser.items.function.CyanFunctionDeclaration
import cyan.compiler.parser.items.expression.CyanExpression

class StackFrame {

    val localVariables = mutableMapOf<String, CyanExpression?>()

    val scopedFunctions = mutableMapOf<String, CyanFunctionDeclaration>()

}
