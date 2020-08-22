package cyan.compiler.parser

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.operator.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled

@Suppress("RemoveRedundantBackticks")
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
    inner class Binary {

        @Test fun `simple addition`() = doTest (
            """
                2 + 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryPlusOperator,
                CyanNumericLiteralExpression(5)
            )
        )

        @Test fun `simple subtraction`() = doTest (
            """
                2 - 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryMinusOperator,
                CyanNumericLiteralExpression(5)
            )
        )

        @Test fun `subtraction with negative operands #1`() = doTest (
            """
                -2 - 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(-2),
                CyanBinaryMinusOperator,
                CyanNumericLiteralExpression(5)
            )
        )

        @Test fun `subtraction with negative operands #2`() = doTest (
            """
                -2 - -5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(-2),
                CyanBinaryMinusOperator,
                CyanNumericLiteralExpression(-5)
            )
        )

        @Test fun `subtraction with negative operands #3`() = doTest (
            """
                2 - -5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryMinusOperator,
                CyanNumericLiteralExpression(-5)
            )
        )

        @Test fun `simple multiplication`() = doTest (
            """
                2 * 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryTimesOperator,
                CyanNumericLiteralExpression(5)
            )
        )

        @Test fun `simple division`() = doTest (
            """
                2 / 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryDivOperator,
                CyanNumericLiteralExpression(5)
            )
        )

        @Disabled @Test fun `simple exponentiation`() = doTest (
            """
                2 ^ 5
            """.trimIndent(),
            CyanBinaryExpression (
                CyanNumericLiteralExpression(2),
                CyanBinaryExpOperator,
                CyanNumericLiteralExpression(5)
            )
        )

    }

    @Nested
    inner class Arrays {

        @Test fun `no elements`() = doTest (
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
    inner class Members {

        @Test fun `reference and reference`() = doTest (
            """
                parent.child
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanIdentifierExpression("parent"),
                CyanIdentifierExpression("child")
            )
        )

        @Test fun `three references`() = doTest (
            """
                parent.child.grandChild
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanMemberAccessExpression (
                    CyanIdentifierExpression("parent"),
                    CyanIdentifierExpression("child")
                ),
                CyanIdentifierExpression("grandChild")
            )
        )

        @Test fun `base is function call`() = doTest (
            """
                doSomething(something).member
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanFunctionCall (
                    CyanIdentifierExpression("doSomething"),
                    arrayOf (
                        CyanFunctionCall.Argument(null, CyanIdentifierExpression("something"))
                    )
                ),
                CyanIdentifierExpression("member")
            )
        )

        @Test fun `base is double function call`() = doTest (
            """
                other().doSomething(something).member
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanFunctionCall (
                    CyanMemberAccessExpression (
                        CyanFunctionCall (
                            CyanIdentifierExpression("other"),
                            emptyArray()
                        ),
                        CyanIdentifierExpression("doSomething")
                    ),
                    arrayOf (
                        CyanFunctionCall.Argument (
                            null, CyanIdentifierExpression("something")
                        )
                    )
                ),
                CyanIdentifierExpression("member")
            )
        )

        @Test fun `middle function call`() = doTest (
            """
                thing.doSomething(something).member
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanFunctionCall (
                    CyanMemberAccessExpression (
                        CyanIdentifierExpression("thing"),
                        CyanIdentifierExpression("doSomething")
                    ),
                    arrayOf (
                        CyanFunctionCall.Argument (
                            null, CyanIdentifierExpression("something")
                        )
                    )
                ),
                CyanIdentifierExpression("member")
            )
        )

        @Test fun `base is array index`() = doTest (
            """
                array[0].member
            """.trimIndent(),
            CyanMemberAccessExpression (
                CyanArrayIndexExpression (
                    CyanIdentifierExpression("array"),
                    CyanNumericLiteralExpression(0)
                ),
                CyanIdentifierExpression("member")
            )
        )

    }

    @Nested
    inner class Indexes {

        @Test fun `simple`() = doTest (
            """
                array[0]
            """.trimIndent(),
            CyanArrayIndexExpression (
                CyanIdentifierExpression("array"),
                CyanNumericLiteralExpression(0)
            )
        )

        @Test fun `on member access`() = doTest (
            """
                array.subArray[0]
            """.trimIndent(),
            CyanArrayIndexExpression (
                CyanMemberAccessExpression (
                    CyanIdentifierExpression("array"),
                    CyanIdentifierExpression("subArray")
                ),
                CyanNumericLiteralExpression(0)
            )
        )

        @Disabled @Test fun `index of index`() = doTest (
            """
                array[0][1]
            """.trimIndent(),
            CyanArrayIndexExpression (
                CyanArrayIndexExpression (
                    CyanIdentifierExpression("array"),
                    CyanNumericLiteralExpression(0)
                ),
                CyanNumericLiteralExpression(1)
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

        @Test fun `nested calls`() = doTest (
            """
                a(1, b(x), 2)
            """.trimIndent(),
            CyanFunctionCall (
                CyanIdentifierExpression("a"),
                arrayOf (
                    CyanFunctionCall.Argument(null, CyanNumericLiteralExpression(1)),
                    CyanFunctionCall.Argument (
                        null,
                        CyanFunctionCall (
                            CyanIdentifierExpression("b"),
                            arrayOf (
                                CyanFunctionCall.Argument(null, CyanIdentifierExpression("x"))
                            )
                        )
                    ),
                    CyanFunctionCall.Argument(null, CyanNumericLiteralExpression(2))
                )
            )
        )

        @Test fun `member access`() = doTest (
            """
                greeting.show()
            """.trimIndent(),
            CyanFunctionCall (
                CyanMemberAccessExpression (
                    CyanIdentifierExpression("greeting"),
                    CyanIdentifierExpression("show")
                ),
                emptyArray()
            )
        )

    }

}
