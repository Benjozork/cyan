package cyan.interpreter.resolver

import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.interpreter.evaluator.values.CyanValue

interface ResolveScope {

    fun findByIdentifier(ident: CyanIdentifierExpression): CyanValue<out Any>?

}
