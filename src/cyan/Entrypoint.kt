package cyan

import cyan.compiler.parser.CyanSourceParser
import cyan.interpreter.Interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import cyan.compiler.parser.items.CyanSource
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
fun main() {
    println("welcome to cyanide v0.1.0, running cyan v0.1.0\n")

    println("parsing source ...")

    var source: CyanSource
    val timeTakenToParse = measureTime {
        source = CyanSourceParser().parseToEnd("""
            |let a = 1847899 + 301111
            |print(5 + 5)
            |print(5 - 7)
            |print(a)
            |hi(a) {
            |   print(a)
            |}
            |hi(9)
            """.trimMargin())
    }

    println("parsing source took ${timeTakenToParse.inMilliseconds} ms\n")

    val interpreter = Interpreter()

    val timeTakenToInterpret = measureTime {
        interpreter.run(source)
    }

    println("\ninterpreting code took ${timeTakenToInterpret.inMilliseconds} ms\n")
}
