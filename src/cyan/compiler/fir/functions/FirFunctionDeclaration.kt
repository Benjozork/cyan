package cyan.compiler.fir.functions

import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.parser.ast.function.CyanFunctionAttribute
import cyan.compiler.parser.ast.function.CyanFunctionDeclaration

class FirFunctionDeclaration (
    override val parent: FirNode,
    override val name: String,
    val returnType: Type,
    val isExtern: Boolean,
    var args: Array<FirFunctionArgument>,
    val fromAstNode: CyanFunctionDeclaration
): FirScope, FirSymbol, FirReflectedElement {

   sealed class Attribute(override val parent: FirNode, open val ident: FirReference, val fromAstNode: CyanFunctionAttribute): FirNode {

       class Keyword(parent: FirNode, ident: FirReference, fromAstNode: CyanFunctionAttribute): Attribute(parent, ident, fromAstNode) {

           override fun allReferredSymbols() = emptySet<FirResolvedReference>()

       }

       class Value(parent: FirNode, ident: FirReference, fromAstNode: CyanFunctionAttribute): Attribute(parent, ident, fromAstNode) {

           lateinit var expr: FirExpression

           override fun allReferredSymbols() = expr.allReferredSymbols()

       }

   }

    override val isInheriting = false

    val attributes = mutableListOf<Attribute>()

    var receiver: FirFunctionReceiver? = null

    lateinit var block: FirSource

    override fun allReferredSymbols() = block.allReferredSymbols()

    override val declaredSymbols get() = (args.toList() + this + receiver).filterNotNull().toMutableSet()

    override val reflectedStructName = "Function"

}
