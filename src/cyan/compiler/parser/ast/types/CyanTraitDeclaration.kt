package cyan.compiler.parser.ast.types

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

class CyanTraitDeclaration (
    val name: CyanIdentifierExpression,
    val elements: Array<Element>,
    override val span: Span? = null
) : CyanStatement {

    sealed class Element : CyanItem {

        class Function(val functionDeclaration: CyanFunctionDeclaration, override val span: Span? = null) : Element() {

            override fun toString() = functionDeclaration.toString().removeSuffix("{ ... }")

        }

        class Property(val name: CyanIdentifierExpression, val type: CyanTypeAnnotation, override val span: Span? = null): Element() {

            override fun toString() = "$name: $type"

        }

    }

    override fun toString() = """
        |trait {
        |    ${elements.joinToString(separator = "\n    ")}
        |}
    """.trimMargin()

}
