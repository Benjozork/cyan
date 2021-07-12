package cyan.compiler.codegen.wasm.lower

import cyan.compiler.codegen.FirItemLower
import cyan.compiler.codegen.wasm.WasmLoweringContext
import cyan.compiler.codegen.wasm.dsl.*
import cyan.compiler.common.types.CyanType
import cyan.compiler.common.types.Type
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration

object WasmInlineInstructionsFunctionLower : FirItemLower<WasmLoweringContext, FirFunctionDeclaration, Wasm.OrderedElement> {

    override fun lower(context: WasmLoweringContext, item: FirFunctionDeclaration): Wasm.OrderedElement {
        val instructionsAttribute = item.attributes.find { it.ident.text == "wasm_instructions" } as? FirFunctionDeclaration.Attribute.Value

        require (instructionsAttribute != null)
        require (instructionsAttribute.expr is FirExpression.Literal.Array && instructionsAttribute.expr.type() == Type.Primitive(CyanType.Str, true))

        val functionName = item.name
        val isStartExport = item.name == "_start"

        val wasmFunctionParameters = (item.args.toList() + item.receiver?.let { FirFunctionArgument(item, "_r", it.type) })
            .filterNotNull().map { arg -> WasmFunction.Parameter(arg.name, Wasm.Type.i32) }.toTypedArray()

        val wasmReturnType = when (item.returnType) {
            Type.Primitive(CyanType.I32),
            Type.Primitive(CyanType.Bool),
            Type.Primitive(CyanType.Str),
            is Type.Struct -> Wasm.Type.i32
            Type.Primitive(CyanType.Void) -> null
            else -> error("fir2wasm: cyan return type '${item.returnType}' not supported yet")
        }

        val instructions = (instructionsAttribute.expr as FirExpression.Literal.Array).elements.map { it as FirExpression.Literal.String }

        return func(functionName, *wasmFunctionParameters, returnType = wasmReturnType, exportedAs = if (isStartExport) "_start" else null) {
            if (isStartExport)
                call("cy_init_heap")

            for (attributeInstruction in instructions) {
                +Wasm.Instruction(attributeInstruction.value)
            }

            for (localNum in context.locals.values) {
                local.new(localNum, Wasm.Type.i32)
            }
        }
    }

}
