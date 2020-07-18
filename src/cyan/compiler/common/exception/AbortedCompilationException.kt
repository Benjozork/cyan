package cyan.compiler.common.exception

class AbortedCompilationException(message: String, cause: Exception? = null): Exception(message, cause)
