package cyan.interpreter.evaluator

class CyanStringValue(override val value: String) : CyanValue<String> {

    override fun toString() = value

}
