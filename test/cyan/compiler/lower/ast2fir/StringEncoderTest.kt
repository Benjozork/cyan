package cyan.compiler.lower.ast2fir

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import cyan.compiler.lower.ast2fir.expression.string.StringContent
import cyan.compiler.lower.ast2fir.expression.string.StringContentParser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested

class StringContentParserTest {

    private val parser = StringContentParser()

    private fun doTest(original: String, expectedNodes: List<StringContent.Node>) {
        val content = parser.parseToEnd(original)

        for (node in expectedNodes) {
            assertEquals(expectedNodes, content.nodes)
        }
    }

    @Nested
    inner class Newlines {

        @Test fun single() =
            doTest("Hello, world!\\n", listOf (
                StringContent.Text("Hello, world!"),
                StringContent.Escape("\n")
            ))

        @Test fun multiple() =
            doTest("Hello, world!\\n\\n", listOf (
                StringContent.Text("Hello, world!"),
                StringContent.Escape("\n"),
                StringContent.Escape("\n")
            ))

    }

    @Nested
    inner class Tabs {

        @Test fun single() =
            doTest("Rent:\\t", listOf (
                StringContent.Text("Rent:"),
                StringContent.Escape("\t")
            ))

        @Test fun multiple() =
            doTest("Rent:\\t\\t", listOf (
                StringContent.Text("Rent:"),
                StringContent.Escape("\t"),
                StringContent.Escape("\t")
            ))

    }

    @Nested
    inner class Backslashes {

        @Test fun single() =
            doTest("Backslash:\\\\", listOf (
                StringContent.Text("Backslash:"),
                StringContent.Escape("\\")
            ))

        @Test fun multiple() =
            doTest("Backslash:\\\\\\\\", listOf (
                StringContent.Text("Backslash:"),
                StringContent.Escape("\\"),
                StringContent.Escape("\\")
            ))

    }

}
