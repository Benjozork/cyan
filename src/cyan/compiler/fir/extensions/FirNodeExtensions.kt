package cyan.compiler.fir.extensions

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.fir.*
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.types.CyanTypeAnnotation

import kotlin.reflect.full.isSubclassOf

fun FirNode.findSymbol(reference: FirReference): FirSymbol? {
    return if (this is FirScope) {
        this.declaredSymbols.firstOrNull { it.name == reference.text }
            ?: this.parent?.findSymbol(reference)
    } else this.parent?.findSymbol(reference)
}

fun FirNode.resolveType(typeAnnotation: CyanTypeAnnotation, inAstNode: CyanItem? = null): Type {
    return when (typeAnnotation) {
        is CyanTypeAnnotation.Literal -> typeAnnotation.literalType
        is CyanTypeAnnotation.Reference -> {
            val typeSymbol = findSymbol(FirReference(this, typeAnnotation.identifierExpression.value))
                ?: DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Unresolved symbol '${typeAnnotation.identifierExpression.value}'",
                        astNode = inAstNode ?: typeAnnotation,
                        span = typeAnnotation.span
                    )
                )

            if (typeSymbol !is FirTypeDeclaration) {
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "Symbol '${typeSymbol.name}' is not a type",
                        astNode = inAstNode ?: typeAnnotation
                    )
                )
            }

            typeSymbol.struct
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
 * Find the first [FirScope] in the ancestors of [this]
 */
fun FirNode.containingScope() = this.firstAncestorOfType<FirScope>()
