package cyan

import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.parser.CyanSourceParser
import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.fir.FirDocument
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.interpreter.Interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
fun main() {
    println("welcome to cyanide v0.1.0, running cyan v0.1.0\n")

    println("parsing source ...")

    var source: CyanSource
    val timeTakenToParse = measureTime {
        source = CyanSourceParser().parseToEnd("""
            |let a = 1847899 + (301111 * 5)
            |let b = "hello"
            |let c = ["hi", "hello", b]
            |let d = true
            |if (d) {
            |   print(c)
            |} else if (false || d) {
            |   print("hi !")
            |} else {
            |   print("ho !")
            |}
            |function hi(a) {
            |   let array = [1, 3, 42, 127, (10 % 3)]
            |   print("Hello world !")
            |   print(array)
            |   print(array.length)
            |}
            |hi(9)
            |print(hi)
            """.trimMargin())
    }

    val fir = SourceLower.lower(source, FirDocument())
    println("symbols: ${fir.declaredSymbols}")

    println("parsing source took ${timeTakenToParse.inMilliseconds} ms\n")

    val interpreter = Interpreter()

    val timeTakenToInterpret = measureTime {
        interpreter.run(source)
    }

    println("\ninterpreting code took ${timeTakenToInterpret.inMilliseconds} ms\n")

    var jsSource: String
    val timeTakenToTranslate =  measureTime {
        jsSource = JsCompilerBackend().translateSource(source, isRoot = true)
    }

    println(jsSource)

    println("\ncompiling code to js took ${timeTakenToTranslate.inMilliseconds} ms\n")
}
