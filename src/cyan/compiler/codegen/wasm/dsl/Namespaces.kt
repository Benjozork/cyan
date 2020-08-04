@file:Suppress("FunctionName")

package cyan.compiler.codegen.wasm.dsl

object Int32Instructions

object LocalInstructions

@WasmTypesNamesDsl
val WasmBlock.i32 get() = Int32Instructions

@WasmTypesNamesDsl
val WasmBlock.local get() = LocalInstructions
