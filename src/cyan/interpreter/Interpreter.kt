package cyan.interpreter

import cyan.compiler.parser.models.CyanSource

class Interpreter {

    fun iprintln(msg: String) = println("interpreter > $msg")

    fun run(source: CyanSource) {
        source.declarations.forEach { vd ->
            iprintln("Variable '${vd.name}' has value ${vd.value} !")
        }
    }

}
