package cyan.compiler.codegen

import cyan.compiler.fir.FirNode

interface FirItemLower<TLoweringContext: LoweringContext, TItem : FirNode, TOuput : Any> {

    fun lower(context: TLoweringContext, item: TItem): TOuput

}
