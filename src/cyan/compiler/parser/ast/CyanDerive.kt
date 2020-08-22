package cyan.compiler.parser.ast

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionArgument
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

class CyanDerive (
    val traitAnnotation: CyanTypeAnnotation.Reference,
    val impls: Array<Item>,
    override val span: Span? = null
) : CyanStatement {

    sealed class Item(val name: CyanIdentifierExpression) : CyanItem {

        class Function (
            name: CyanIdentifierExpression,
            val args: Array<CyanFunctionArgument>,
            val returnType: CyanTypeAnnotation,
            val body: CyanSource,
            override val span: Span? = null
        ) : Item(name) {

            override fun toString() = """
                $name(${args.joinToString(", ")}): $returnType { ... }
            """.trimIndent()

        }

    }

    override fun toString() = """
        derive ${traitAnnotation.identifierExpression.value} {
            ${impls.joinToString("\n    ")}
        }
    """.trimIndent()

}
