package cyan.compiler.codegen.wasm

import cyan.compiler.codegen.LoweringContext

class WasmLoweringContext(override val backend: WasmCompilerBackend) : LoweringContext
