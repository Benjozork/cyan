package cyan.compiler.parser

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.parser.ast.expression.CyanArrayExpression
import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled

class ExpressionsTest {

    private val parser = object : Grammar<CyanExpression>() {

        private val sp = CyanModuleParser()

        override val tokens = sp.tokens

        override val rootParser = sp.expr

    }

    private fun doTest(source: String, expectedExpr: CyanExpression) =
            assertEquals(expectedExpr.toString(), parser.parseToEnd(source).toString())

    @Nested
    inner class Primitives {

        @Test fun string() = doTest (
            """
                "Hello, World !"
            """.trimIndent(),
            CyanStringLiteralExpression("Hello, World !")
        )

        @Test fun `numeric int`() = doTest (
            """
                32
            """.trimIndent(),
            CyanNumericLiteralExpression(32)
        )

        @Test fun `boolean true`() = doTest (
            """
                true
            """.trimIndent(),
            CyanBooleanLiteralExpression(true)
        )

        @Test fun `boolean false`() = doTest (
            """
                false
            """.trimIndent(),
            CyanBooleanLiteralExpression(false)
        )

    }

    @Nested
    inner class Arrays {

        @Disabled @Test fun `no elements`() = doTest (
            """
                []
            """.trimIndent(),
            CyanArrayExpression (
                emptyArray()
            )
        )

        @Test fun `one element`() = doTest (
            """
                ["hi"]
            """.trimIndent(),
            CyanArrayExpression (
                arrayOf (
                    CyanStringLiteralExpression("hi")
                )
            )
        )

        @Test fun `multiple elements`() = doTest (
            """
                ["hi", "hello"]
            """.trimIndent(),
            CyanArrayExpression (
                arrayOf (
                    CyanStringLiteralExpression("hi"),
                    CyanStringLiteralExpression("hello")
                )
            )
        )

        @Test fun nested() = doTest (
            """
                ["hi", ["hello", "world"]]
            """.trimIndent(),
            CyanArrayExpression (
                arrayOf (
                    CyanStringLiteralExpression("hi"),
                    CyanArrayExpression (
                        arrayOf (
                            CyanStringLiteralExpression("hello"),
                            CyanStringLiteralExpression("world")
                        )
                    )
                )
            )
        )

    }

    @Nested
    inner class FunctionCalls {

        @Test fun `no arguments`() = doTest (
            """
                Person()
            """.trimIndent(),
            CyanFunctionCall (
                CyanIdentifierExpression("Person"),
                emptyArray()
            )
        )

        @Test fun `positional arguments`() = doTest (
            """
                Person("a", 18)
            """.trimIndent(),
            CyanFunctionCall (
                CyanIdentifierExpression("Person"),
                arrayOf (
                    CyanFunctionCall.Argument(null, CyanStringLiteralExpression("a")),
                    CyanFunctionCall.Argument(null, CyanNumericLiteralExpression(18))
                )
            )
        )

        @Test fun `named arguments`() = doTest (
            """
                Person(name: "a", age: 18)
            """.trimIndent(),
            CyanFunctionCall (
                CyanIdentifierExpression("Person"),
                arrayOf (
                    CyanFunctionCall.Argument(CyanIdentifierExpression("name"), CyanStringLiteralExpression("a")),
                    CyanFunctionCall.Argument(CyanIdentifierExpression("age"), CyanNumericLiteralExpression(18))
                )
            )
        )

    }

}
