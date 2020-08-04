package cyan.compiler.codegen

interface LoweringContext {

    val backend: FirCompilerBackend<*>

}
