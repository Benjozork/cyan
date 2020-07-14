package cyan.interpreter.runtime

import cyan.interpreter.evaluator.values.CyanArrayValue
import cyan.interpreter.evaluator.values.CyanNumberValue
import cyan.interpreter.evaluator.values.CyanValue

import kotlin.reflect.KClass

object Builtins {

    val functions = mapOf<KClass<out CyanValue<*>>, Map<String, (value: CyanValue<*>) -> CyanValue<*>>> (
        CyanArrayValue::class to mapOf (
            "length" to { array -> CyanNumberValue((array as CyanArrayValue<*>).elements.size) }
        )
    )

}
