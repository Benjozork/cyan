package cyan

import cyan.compiler.parser.CyanSourceParser
import cyan.compiler.parser.ast.CyanSource
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.fir.FirDocument
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.compiler.codegen.js.JsCompilerBackend
import cyan.compiler.codegen.wasm.WasmCompilerBackend
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
            |type Person = struct {
            |   name: str,
            |   age: i32
            |}
            |let a = 1847899 + (301111 * 5)
            |var b = "hello"
            |print(b)
            |b = "hi !"
            |print(b)
            |let c = ["hi", "hello", b]
            |let d: bool = true
            |let e = c[0]
            |let f = b
            |let g: Person = { "James", 18 }
            |print(g)
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
        jsSource = JsCompilerBackend().translateSource(fir, isRoot = true)
    }

    println(jsSource)

    println("\ncompiling code to js took ${timeTakenToTranslate.inMilliseconds} ms\n")

    val backend = WasmCompilerBackend()

    val wasmStrExp = backend.lowerExpression(FirExpression(FirDocument(), CyanStringLiteralExpression("Hello, world ! I will allocate too much !")))
    val oneOtherExp = backend.lowerExpression(FirExpression(FirDocument(), CyanStringLiteralExpression("Test of alloc() !")))
    println("heap: ${backend.heap.joinToString { "0x${it.toString(16)}" }}")
    println("exprs: $wasmStrExp, $oneOtherExp")
}
