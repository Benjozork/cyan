package cyan

import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.parser.CyanSourceParser
import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.fir.FirDocument
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.interpreter.Interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import java.io.File

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
fun main() {
//    println("welcome to cyanide v0.1.0, running cyan v0.1.0\n")

    val parser = CyanSourceParser()

    val runtimeSource = parser.parseToEnd(File("runtime/runtime.cy").readText())
    val runtimeFirDocument = SourceLower.lower(runtimeSource, FirDocument())

    var source: CyanSource
    val timeTakenToParse = measureTime {
        source = parser.parseToEnd("""
            |let a = 1847899 + (301111 * 5)
            |let b = "hello"
            |let c = ["hi", "hello", b]
            |let d: bool = true
            |let e = c[0]
            |let f = b
            |if (d) {
            |    print(c[2])
            |} else if (false || d) {
            |    print("hi !")
            |} else {
            |    print("ho !")
            |}
            |function hello(a: str) {
            |   print("Hello, stranger ! here's the value")
            |   print(a)
            |}
            |hello("<dumb value>>")
            """.trimMargin())
    }

    val fir = SourceLower.lower(source, FirDocument(runtimeFirDocument.declaredSymbols))
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
