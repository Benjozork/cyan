package cyan.interpreter.runtime

import cyan.interpreter.evaluator.CyanArrayValue
import cyan.interpreter.evaluator.CyanNumberValue
import cyan.interpreter.evaluator.CyanValue

@Suppress("FunctionName")
object Intrinsics {

    fun <TElement : CyanValue<Any>> ArrayGet(array: CyanArrayValue<TElement>, index: CyanNumberValue) =
            array.elements[index.value]

}
