package cyan.interpreter.evaluator.values

class CyanBooleanValue(override val value: Boolean) : CyanValue<Boolean> {

    override fun toString() = value.toString()

}
