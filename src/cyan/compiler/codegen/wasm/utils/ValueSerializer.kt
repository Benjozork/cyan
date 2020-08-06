package cyan.compiler.codegen.wasm.utils

import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.expression.FirExpression

/**
 * Temporary utils for converting constant values to strings
 */
object ValueSerializer {

    fun convert(expression: FirExpression): String { // poor man's itostr
        return when (expression) {
            is FirExpression.Literal.String -> expression.value
            is FirExpression.Literal.Number -> expression.value.toString()
            is FirExpression.Literal.Array  -> expression.elements.joinToString(", ", "[", "]") { convert(it) }
            is FirExpression.Literal.Struct -> expression.elements.entries.joinToString(", ", "{ ", " }") { "${it.key.name}: ${convert(it.value)}" }
            is FirResolvedReference -> when (val symbol = expression.resolvedSymbol) {
                is FirVariableDeclaration -> convert(symbol.initializationExpr)
                else -> error("fir2wasm-print-formatter: cannot format reference to '${expression::class.simpleName}'")
            }
            else -> error("fir2wasm-print-formatter: cannot format value of type '${expression::class.simpleName}'")
        }
    }

}
