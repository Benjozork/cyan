package cyan.interpreter.evaluator.values

class CyanNumberValue(override val value: Int) : CyanValue<Int> {

    override fun toString() = value.toString()

}
