package cyan.compiler

import cyan.compiler.parser.VariableListParser

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main() {
    val result = VariableListParser().parseToEnd("let a, let b, let c")

    println(result)
}
