package cyan.interpreter.runtime

import cyan.interpreter.evaluator.values.CyanArrayValue
import cyan.interpreter.evaluator.values.CyanNumberValue
import cyan.interpreter.evaluator.values.CyanValue

@Suppress("FunctionName")
object Intrinsics {

    fun <TElement : CyanValue<Any>> ArrayGet(array: CyanArrayValue<TElement>, index: CyanNumberValue) =
            array.elements[index.value]

}
