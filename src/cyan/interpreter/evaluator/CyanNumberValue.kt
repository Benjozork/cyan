package cyan.interpreter.evaluator

class CyanNumberValue(override val value: Int) : CyanValue<Int> {

    override fun toString() = value.toString()

}
