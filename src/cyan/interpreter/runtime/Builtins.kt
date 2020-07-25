package cyan.interpreter.runtime

import cyan.interpreter.evaluator.values.CyanArrayValue
import cyan.interpreter.evaluator.values.CyanNumberValue
import cyan.interpreter.evaluator.values.CyanValue
import cyan.interpreter.ierror

import kotlin.reflect.KClass

object Builtins {

    val functions = mapOf<KClass<out CyanValue<*>>, Map<String, (value: CyanValue<*>, agrs: Array<CyanValue<*>>) -> CyanValue<*>>> (
        CyanArrayValue::class to mapOf (
            "length" to { array, _ -> CyanNumberValue((array as CyanArrayValue<*>).elements.size) },
            "index"  to { array, a ->
                val index = (a.first() as? CyanNumberValue) ?: ierror("array index must be number")

                (array as CyanArrayValue<*>).elements[index.value]
            }
        )
    )

}
