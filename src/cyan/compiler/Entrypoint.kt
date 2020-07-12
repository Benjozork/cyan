package cyan.compiler

import cyan.compiler.parser.CyanSourceParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main() {
    val result = CyanSourceParser().parseToEnd("""
    |let a = 1847899
    |let b = 6
    |let c
    |let d
    |let e = 4
    """.trimMargin())

    println(result)
}
