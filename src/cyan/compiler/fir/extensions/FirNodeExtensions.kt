package cyan.compiler.fir.extensions

import cyan.compiler.fir.*
import kotlin.reflect.full.isSubclassOf

fun FirNode.findSymbol(reference: FirReference): FirSymbol? {
    return if (this is FirScope) {
        this.declaredSymbols.firstOrNull { it.name == reference.text }
            ?: this.parent?.findSymbol(reference)
    } else this.parent?.findSymbol(reference)
}

inline fun <reified TAncestor : FirNode> FirNode.firstAncestorOfType(): TAncestor? {
    var nextParent: FirNode? = this
    while (nextParent != null) {
        if (nextParent::class.isSubclassOf(TAncestor::class))
            return nextParent as TAncestor

        nextParent = nextParent.parent
    }
    return null
}
