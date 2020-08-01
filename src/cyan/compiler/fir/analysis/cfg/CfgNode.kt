package cyan.compiler.fir.analysis.cfg

import cyan.compiler.fir.FirIfChain
import cyan.compiler.fir.FirNode

sealed class CfgNode {

    lateinit var fromFirNode: FirNode

    open class Forwarding : CfgNode() {
        var nextNode: CfgNode? = null
    }

    class Conditional : CfgNode() {
        var trueNode: CfgNode? = null
        var falseNode: CfgNode? = null
        var exitNode: Exit? = null
    }

    class Exit : Forwarding()

    override fun toString() = toString(0, false)

    fun nextWithFirNode(firNode: FirNode): CfgNode? {
        return if (this.fromFirNode == firNode) this
        else when (this) {
            is Forwarding -> this.nextNode?.nextWithFirNode(firNode)
            is Conditional -> this.trueNode?.nextWithFirNode(firNode)
                    ?: this.falseNode?.nextWithFirNode(firNode)
                    ?: this.exitNode?.nextWithFirNode(firNode)
        }
    }

    fun toString(pos: Int, isInBranch: Boolean): String {
        var base = "#$pos [${fromFirNode::class.simpleName}]\n"

        when (this) {
            is Forwarding -> {
                if (this is Exit) return when {
                    isInBranch -> ""
                    else -> "#$pos <join>"
                }
                else base += nextNode?.toString(pos + 1, isInBranch) ?: ""
            }
            is Conditional -> {
                base += "\tcond: ${(fromFirNode as FirIfChain).branches.first().first.realExpr}\n"
                base += "\ttrue:\n${trueNode?.toString(0, true)!!.prependIndent("\t\t").removeSuffix("\t")}"
                base += "false: ${falseNode?.toString(0, true) ?: "<none>"}\n"
                base += exitNode?.toString(pos + 1, false) + "\n"
            }
        }

        return base
    }

}
