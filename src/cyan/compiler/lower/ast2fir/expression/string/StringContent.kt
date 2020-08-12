package cyan.compiler.lower.ast2fir.expression.string

class StringContent(val nodes: List<Node>) {

    interface Node

    data class Text(val content: String) : Node

    data class Escape(val character: String) : Node

    override fun toString() = nodes.joinToString("") {
        when (it) {
            is Text -> it.content
            is Escape -> it.character
            else -> error("bad node type '${it::class.simpleName}'")
        }
    }

}
