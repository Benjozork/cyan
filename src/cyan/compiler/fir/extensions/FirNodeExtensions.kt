package cyan.compiler.fir.extensions

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

import kotlin.reflect.full.isSubclassOf

fun FirNode.findSymbol(reference: FirReference) = findSymbol(reference, reference.parent)

fun FirNode.findSymbol(reference: FirReference, asParentOf: FirNode): FirResolvedReference? {
    return if (this is FirScope) {
        this.declaredSymbols.firstOrNull { it.name == reference.text }?.let { FirResolvedReference(reference.parent, it, reference.text, reference.fromAstNode) }
            ?: this.parent?.findSymbol(reference, asParentOf)
    } else this.parent?.findSymbol(reference, asParentOf)
}

fun FirNode.resolveType(typeAnnotation: CyanTypeAnnotation, inAstNode: CyanItem? = null): Type {
    return when (typeAnnotation) {
        is CyanTypeAnnotation.Literal -> typeAnnotation.literalType
        is CyanTypeAnnotation.Reference -> {
            val typeReference = FirReference(this, typeAnnotation.identifierExpression.value, typeAnnotation.identifierExpression)

            if (typeReference.text.startsWith("self"))
                return Type.Self(typeReference.text.endsWith("[]"))

            val typeSymbol = findSymbol(typeReference, this)
                ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Unresolved symbol '${typeAnnotation.identifierExpression.value}'",
                        astNode = inAstNode ?: typeAnnotation, span = typeAnnotation.span
                    )
                )

            if (typeSymbol.resolvedSymbol !is FirTypeDeclaration.Struct && typeSymbol.resolvedSymbol !is FirTypeDeclaration.Trait) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Symbol '${typeSymbol.resolvedSymbol.name}' is not a struct or trait type",
                        astNode = inAstNode ?: typeAnnotation, span = typeAnnotation.span
                    )
                )
            }

            (typeSymbol.resolvedSymbol as FirTypeDeclaration<*>).type
        }
    }
}

/**
 * Find the first [FirNode] of type [TAncestor] in the ancestors of [this]
 */
inline fun <reified TAncestor : FirNode> FirNode.firstAncestorOfType(): TAncestor? {
    var nextParent: FirNode? = this
    while (nextParent != null) {
        if (nextParent::class.isSubclassOf(TAncestor::class))
            return nextParent as TAncestor

        nextParent = nextParent.parent
    }
    return null
}

/**
 * Find the containing [FirModuleRoot]
 */
fun FirNode.moduleRoot(): FirModuleRoot =
    firstAncestorOfType()!!

/**
 * Finds the containing [cyan.compiler.mir.Module]
 */
fun FirNode.module() =
    moduleRoot().mirModule

/**
 * Find the first [FirScope] in the ancestors of [this]
 */
fun FirNode.containingScope() = this.firstAncestorOfType<FirScope>()
