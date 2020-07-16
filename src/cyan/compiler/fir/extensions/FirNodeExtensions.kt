package cyan.compiler.fir.extensions

import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirReference
import cyan.compiler.fir.FirScope
import cyan.compiler.fir.FirSymbol

fun FirNode.findSymbol(reference: FirReference): FirSymbol? {
    return if (this is FirScope) {
        this.declaredSymbols.firstOrNull { it.name == reference.text }
            ?: this.parent?.findSymbol(reference)
    } else this.parent?.findSymbol(reference)
}
