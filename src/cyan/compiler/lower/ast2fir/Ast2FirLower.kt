package cyan.compiler.lower.ast2fir

import cyan.compiler.fir.FirNode
import cyan.compiler.lower.Lower
import cyan.compiler.parser.ast.CyanItem

interface Ast2FirLower<TAstNode : CyanItem, TFirNode : FirNode> : Lower {

    fun lower(astNode: TAstNode, parentFirNode: FirNode): TFirNode

}
