package cyan.interpreter.stack

import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.compiler.parser.ast.expression.CyanExpression

class StackFrame {

    val localVariables = mutableMapOf<String, CyanExpression?>()

    val scopedFunctions = mutableMapOf<String, CyanFunctionDeclaration>()

}
