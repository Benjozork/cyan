package cyan.interpreter.evaluator

class CyanBooleanValue(override val value: Boolean) : CyanValue<Boolean> {

    override fun toString() = value.toString()

}
