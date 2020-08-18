package cyan.compiler.parser.grammar

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken

import cyan.compiler.parser.grammars.NumericLiteralParser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@Suppress("RemoveRedundantBackticks")
class NumericLiteralsTest {

    private val parser = NumericLiteralParser(literalToken("-"))

    private fun doTest(source: String, expected: Int) =
            assertEquals(expected, parser.parseToEnd(source).value)

    @Nested
    inner class Decimal {

        @Test fun `46`() = doTest("46", 46)

        @Test fun `-5`() = doTest("-5", -5)

        @Test fun `0d46`() = doTest("0d46", 46)

        @Test fun `255`() = doTest("255", 255)

        @Test fun `0`() = doTest("0", 0)

        @Test fun `1_000_000`() = doTest("1_000_000", 1_000_000)

        @Test fun `-100_000`() = doTest("-100_000", -100_000)

    }

    @Nested
    inner class Binary {

        @Test fun `0`() = doTest("0b0", 0)

        @Test fun `01`() = doTest("0b01", 1)

        @Test fun `10`() = doTest("0b10", 2)

        @Test fun `111001`() = doTest("0b111001", 57)

        @Test fun `111_000_1_0`() = doTest("0b111_000_10", 226)

        @Test fun `-100_000`() = doTest("-0b100_000", -32)

    }

    @Nested
    inner class Hexadecimal {

        @Test fun `FF`() = doTest("0xFF", 255)

        @Test fun `A`() = doTest("0xA", 10)

        @Test fun `40`() = doTest("0x40", 64)

        @Test fun `FF_FF_FF`() = doTest("0xFF_FF_FF", 16777215)

        @Test fun `-a`() = doTest("-0xA", -10)

    }

}
