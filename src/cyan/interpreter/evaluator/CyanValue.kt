package cyan.interpreter.evaluator

interface CyanValue<TValue : Any> {

    val value: TValue

    override fun toString(): String

}
