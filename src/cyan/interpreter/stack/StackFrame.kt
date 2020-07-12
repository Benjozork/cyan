package cyan.interpreter.stack

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration
import cyan.interpreter.evaluator.CyanValue
import cyan.interpreter.resolver.ResolveScope

class StackFrame : ResolveScope {

    val localVariables = mutableMapOf<String, CyanValue<out Any>>()

    val scopedFunctions = mutableMapOf<String, CyanFunctionDeclaration>()

    override fun findByIdentifier(ident: CyanIdentifierExpression) = localVariables[ident.value]

}
