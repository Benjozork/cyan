package cyan.compiler.codegen

import cyan.compiler.fir.FirNode

interface FirItemLower<TBackend: FirCompilerBackend, TLoweringContext: LoweringContext, TItem : FirNode> {

    fun lower(context: TLoweringContext, item: TItem): String

}
