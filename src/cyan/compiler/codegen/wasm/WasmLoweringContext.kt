package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.LoweringContext
import cyan.compiler.fir.FirVariableDeclaration

class WasmLoweringContext(override val backend: WasmCompilerBackend) : LoweringContext {

    var numLocals = 0

    val pointerForLocal = mutableMapOf<FirVariableDeclaration, Int>()

}
