@file:Suppress("FunctionName")

package cyan.compiler.codegen.wasm.dsl

object Int32Instructions

object LocalInstructions

@WasmTypesNamesDsl
val WasmScope.i32 get() = Int32Instructions

@WasmTypesNamesDsl
val WasmScope.local get() = LocalInstructions
