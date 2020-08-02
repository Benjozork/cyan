package cyan.compiler.codegen.js

import cyan.compiler.codegen.FirCompilerBackend
import cyan.compiler.codegen.LoweringContext

class JsLoweringContext(override val backend: FirCompilerBackend) : LoweringContext
