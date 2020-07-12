package cyan.compiler.codegen

import cyan.compiler.parser.ast.CyanItem

interface ItemLower<TItem : CyanItem> {

    fun lower(backend: CompilerBackend, item: TItem): String

}
