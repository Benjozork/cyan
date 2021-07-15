package cyan.compiler.fir

import cyan.compiler.common.types.Type

interface FirTyped {

    fun type(): Type

}
