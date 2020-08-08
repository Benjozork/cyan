@file:Suppress("FunctionName")

package cyan.compiler.codegen.wasm.dsl

object Int32Instructions

object LocalInstructions

object CyanIntrinsics

@WasmTypesNamesDsl
val WasmScope.i32 get() = Int32Instructions

@WasmTypesNamesDsl
val WasmScope.local get() = LocalInstructions

@WasmTypesNamesDsl
val WasmScope.cy get() = CyanIntrinsics
