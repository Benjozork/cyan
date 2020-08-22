package cyan.compiler.codegen.wasm.utils

import cyan.compiler.common.types.Type

val Type.size get() = when (this) {
    is Type.Primitive -> 4
    is Type.Struct -> this.properties.size * 4
    is Type.Trait -> error("cannot get the size of a trait")
}
