package cyan.cli

import cyan.compiler.parser.CyanSourceParser
import cyan.interpreter.Interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

import java.io.File

fun main(args: Array<String>) {
    val file = args.firstOrNull()?.let { File(it) } ?: error("missing file argument")

    if (!file.exists()) {
        error("cannot find file '${file.path}'")
    }

    val fileText = file.readText()
    val parsedSource = CyanSourceParser().parseToEnd(fileText)

    Interpreter().run(parsedSource)
}
