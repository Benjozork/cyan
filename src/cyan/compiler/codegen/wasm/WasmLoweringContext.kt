package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.LoweringContext
import cyan.compiler.fir.FirVariableDeclaration

class WasmLoweringContext(override val backend: WasmCompilerBackend) : LoweringContext {

    val locals = mutableMapOf<FirVariableDeclaration, Int>()

    var numLocals = 0

    val pointerForLocal = mutableMapOf<FirVariableDeclaration, Int>()

    fun addLocal(declaration: FirVariableDeclaration): Int {
        val n = this.locals.size + 1
        this.locals[declaration] = n
        return n
    }

}
