package cyan.compiler.lower.ast2fir

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.fir.FirNode
import cyan.compiler.fir.FirStructDeclaration
import cyan.compiler.parser.ast.CyanType
import cyan.compiler.parser.ast.types.CyanStructDeclaration

object StructDeclarationLower : Ast2FirLower<CyanStructDeclaration, FirStructDeclaration> {

    override fun lower(astNode: CyanStructDeclaration, parentFirNode: FirNode): FirStructDeclaration {
        if (astNode.properties.map { it.ident.value }.toSet().size != astNode.properties.size) { // Check for duplicate properties
            DiagnosticPipe.report (
                CompilerDiagnostic (
                    level = CompilerDiagnostic.Level.Error,
                    message = "A struct cannot have multiple properties with the same name",
                    astNode = astNode
                )
            )
        }

        astNode.properties
            .firstOrNull { it.type.base == CyanType.Void }?.let { property -> // Check for void properties
                DiagnosticPipe.report (
                    CompilerDiagnostic (
                        level = CompilerDiagnostic.Level.Error,
                        message = "struct property cannot have void type",
                        astNode = property
                    )
                )
            }

        val firStructDeclaration = FirStructDeclaration(parentFirNode, astNode.ident.value)

        firStructDeclaration.properties = astNode.properties.map {
            FirStructDeclaration.Property(firStructDeclaration, it.ident.value, it.type)
        }.toTypedArray()

        return firStructDeclaration
    }

}
