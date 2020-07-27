package cyan.compiler.codegen

import cyan.compiler.fir.FirNode

interface FirItemLower<TBackend: FirCompilerBackend, TItem : FirNode> {

    fun lower(backend: TBackend, item: TItem): String

}
