package cyan.interpreter.resolver

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.interpreter.evaluator.CyanValue

interface ResolveScope {

    fun findByIdentifier(ident: CyanIdentifierExpression): CyanValue<out Any>?

}
