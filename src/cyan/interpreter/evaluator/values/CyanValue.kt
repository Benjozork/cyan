package cyan.interpreter.evaluator.values

interface CyanValue<TValue : Any> {

    val value: TValue

    override fun toString(): String

}
