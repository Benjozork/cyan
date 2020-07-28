package cyan

import cyan.compiler.parser.CyanModuleParser
import cyan.compiler.fir.FirModule
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.lower.ast2fir.SourceLower
import cyan.compiler.codegen.js.JsCompilerBackend

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import java.io.File

fun main() {
    val parser = CyanModuleParser()

    val runtimeModule = parser.parseToEnd(File("runtime/runtime.cy").readText())
    val runtimeFirDocument = SourceLower.lower(runtimeModule.source, FirModule())

    val module = parser.parseToEnd("""
        |module main
        |
        |import math
        |
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
        |let h: i32 = strlen(c[1])
        |print(g.name)
        |print(g)
        |let i: i32 = g.age
        |print(h)
        |if (d) {
        |    print(c[2])
        |} else if (false || d) {
        |    print("hi !")
        |} else {
        |    print("ho !")
        |}
        |function hello(a: str): str {
        |   print("Hello, stranger ! here's the value")
        |   print(a)
        |   return a
        |}
        |print(hello("<dumb value>>"))
        |print(strlen("Hamza"))
        |print(powerOfTwo(5))
        |print(factorial(5))
        """.trimMargin())

    val fir = SourceLower.lower(module.source, FirModule(runtimeFirDocument.declaredSymbols))

    // Remove externs for inserting stdlib translation into js
    runtimeFirDocument.declaredSymbols.removeIf { it is FirFunctionDeclaration && it.isExtern }

    val jsSource = JsCompilerBackend(runtimeFirDocument).translateSource(fir, isRoot = true)

    println(jsSource)
}
