package cyan.interpreter.resolver

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.interpreter.evaluator.CyanValue
import cyan.interpreter.ierror
import cyan.interpreter.iprintln
import cyan.interpreter.stack.StackFrame

object Resolver {

    fun findByIdentifier(ident: CyanIdentifierExpression, stackFrame: StackFrame): CyanValue<out Any> {
        iprintln("resolving identifier '${ident.value}'")
        return stackFrame.findByIdentifier(ident) ?: ierror("could not resolve identifier '${ident.value}'")
    }

}
