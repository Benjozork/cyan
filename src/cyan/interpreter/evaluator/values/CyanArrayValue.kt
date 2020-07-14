package cyan.interpreter.evaluator.values

class CyanArrayValue<TElements : CyanValue<out Any>>(val elements: Array<TElements>) : CyanValue<Array<TElements>> {

    override val value = elements

    override fun toString() = "[${elements.joinToString(", ")}]"

}
