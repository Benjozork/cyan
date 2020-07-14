package cyan.interpreter.evaluator.values

class CyanStringValue(override val value: String) : CyanValue<String> {

    override fun toString() = value

}
