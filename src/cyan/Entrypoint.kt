package cyan

import cyan.compiler.parser.CyanSourceParser
import cyan.interpreter.Interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main() {
    val source = CyanSourceParser().parseToEnd("""
    |let a = 1847899
    |let b = 6
    |let c
    |let d
    |let e = 4
    """.trimMargin())

    val interpreter = Interpreter()

    interpreter.run(source)
}
