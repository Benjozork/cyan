package cyan.compiler.codegen

import cyan.compiler.fir.FirNode

interface FirItemLower<TItem : FirNode> {

    fun lower(backend: FirCompilerBackend, item: TItem): String

}
