package cyan.compiler.parser.ast.types

import cyan.compiler.common.types.Type
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.CyanStatement
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

class CyanStructDeclaration (
    val ident: CyanIdentifierExpression,
    val properties: Array<Property>
) : CyanStatement {

    class Property(val ident: CyanIdentifierExpression, val type: Type): CyanItem {

        override fun toString() = "$ident: $type"

    }

    override fun toString() = """
        |struct {
        |    ${properties.joinToString(separator = ",\n    ")}
        |}
    """.trimMargin()

}
