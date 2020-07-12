package cyan.interpreter.stack

import cyan.compiler.parser.items.expression.CyanExpression

class StackFrame {

    val localVariables = mutableMapOf<String, CyanExpression?>()

}
