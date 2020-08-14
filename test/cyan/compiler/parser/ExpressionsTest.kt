package cyan.compiler.parser

import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.parser.ast.expression.CyanExpression
import cyan.compiler.parser.ast.expression.CyanStructLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Assertions.*

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
    inner class StructLiterals {

        @Test fun `inferred type`() = doTest (
            """
                { "a", 18 }
            """.trimIndent(),
            CyanStructLiteralExpression (
                arrayOf (
                    CyanStringLiteralExpression("a"),
                    CyanNumericLiteralExpression(18)
                )
            )
        )

        @Test fun `explicit type`() = doTest (
            """
                Person { "a", 18 }
            """.trimIndent(),
            CyanStructLiteralExpression (
                arrayOf (
                    CyanStringLiteralExpression("a"),
                    CyanNumericLiteralExpression(18)
                )
            )
        )

    }

}
