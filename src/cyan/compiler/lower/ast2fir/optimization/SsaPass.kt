package cyan.compiler.lower.ast2fir.optimization

import cyan.compiler.fir.FirAssignment
import cyan.compiler.fir.FirResolvedReference
import cyan.compiler.fir.FirSource
import cyan.compiler.fir.FirVariableDeclaration
import cyan.compiler.fir.analysis.cfg.StatementChainCfgBuilder
import cyan.compiler.fir.analysis.definitions.DefinitionChainBuilder
import cyan.compiler.fir.expression.FirExpression
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.parser.ast.expression.CyanIdentifierExpression

import kotlin.random.Random

object SsaPass : FirOptimizationPass {

    private fun FirVariableDeclaration.convertToSsa(source: FirSource) {
        val assignments = containingScope()!!.allReferredSymbols() // Find variable assignments
                .filter { it.resolvedSymbol == this && it.parent is FirAssignment }
                .map { it.parent as FirAssignment }

        // Build a CFG for the source
        val sourceCfg = StatementChainCfgBuilder.build(source)

        // Find CFG nodes that are for a FirAssignment node and associate them with a definition chain
        val assignmentNodes = assignments.mapNotNull { sourceCfg.nextWithFirNode(it) }.associateWith { DefinitionChainBuilder.build(it) }

        // For every assignment and its further usages
        for ((assignment, usages) in assignmentNodes.entries) {
            val originDecl = (assignment.fromFirNode as FirAssignment).targetVariable!!
            val newDeclName = "cy_ssa_var_" + Random.nextInt(0, 10_000).toString()

            // Create a new variable decl, initialize it with the new value of the assignment
            val newDecl = FirVariableDeclaration(assignment.fromFirNode.parent!!, newDeclName, false, originDecl.typeAnnotation)
            newDecl.initializationExpr = (assignment.fromFirNode as FirAssignment).newExpr!!

            // Replace the FirAssignment in the containing scope with the new decl
            originDecl.containingScope()!!.declaredSymbols += newDecl
            (assignment.fromFirNode as FirAssignment).replaceWith(listOf(newDecl))

            // Update all further usages from the old assignment
            for (assignmentUsage in usages.drop(1)) {
                assignmentUsage.fromFirNode.allReferredSymbols()
                        .filter { it.resolvedSymbol == originDecl }
                        .forEach {
                            when (val item = it.parent) {
                                is FirExpression.FunctionCall -> item.callee.resolvedSymbol = newDecl
                                is FirExpression -> item.inlinedExpr = FirResolvedReference(item.parent, newDecl, newDecl.name, CyanIdentifierExpression(newDecl.name))
                                else -> error("resolvedSymbol parent was not a FirFunctionCall or a FirExpression")
                            }
                        }
            }
        }
    }

    override fun run(source: FirSource) {
        val allMutableVars = source.declaredSymbols.filterIsInstance<FirVariableDeclaration>().filter { it.mutable }

        allMutableVars.forEach { it.convertToSsa(source) }
    }

}
