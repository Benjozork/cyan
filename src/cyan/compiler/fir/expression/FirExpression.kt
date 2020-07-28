package cyan.compiler.fir.expression

import cyan.compiler.common.diagnostic.CompilerDiagnostic
import cyan.compiler.common.diagnostic.DiagnosticPipe
import cyan.compiler.common.types.Type
import cyan.compiler.common.types.CyanType
import cyan.compiler.fir.*
import cyan.compiler.fir.functions.FirFunctionArgument
import cyan.compiler.fir.functions.FirFunctionDeclaration
import cyan.compiler.fir.extensions.findSymbol
import cyan.compiler.fir.extensions.firstAncestorOfType
import cyan.compiler.fir.extensions.containingScope
import cyan.compiler.parser.ast.expression.*
import cyan.compiler.parser.ast.expression.literal.CyanBooleanLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanNumericLiteralExpression
import cyan.compiler.parser.ast.expression.literal.CyanStringLiteralExpression
import cyan.compiler.parser.ast.operator.CyanBinaryComparisonOperator
import cyan.compiler.parser.ast.function.CyanFunctionCall
import cyan.compiler.lower.ast2fir.expression.ExpressionLower

import com.andreapivetta.kolor.lightGray

class FirExpression(override val parent: FirNode, val astExpr: CyanExpression) : FirNode {

    /**
     * Infers the type of this FirExpression node.
     */
    fun type(): Type {
         return when (astExpr) {
             is CyanNumericLiteralExpression -> Type.Primitive(CyanType.Int32, false)
             is CyanStringLiteralExpression  -> Type.Primitive(CyanType.Str, false)
             is CyanBooleanLiteralExpression -> Type.Primitive(CyanType.Bool, false)
             is CyanFunctionCall -> {
                 val functionReference = FirReference(this, astExpr.functionIdentifier.value)
                 val resolvedFunction = findSymbol(functionReference)
                     ?: DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "Unresolved symbol '${functionReference.text}'",
                             astNode = astExpr
                         )
                     )

                 if (resolvedFunction !is FirFunctionDeclaration) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "Symbol '${functionReference.text}' is not a function",
                             astNode = astExpr
                         )
                     )
                 }

                 val functionDeclarationArgsToPassedArgs = (resolvedFunction.args zip astExpr.args).toMap()
                     .mapValues { (_, astArg) -> ExpressionLower.lower(astArg, this) }

                 functionDeclarationArgsToPassedArgs.entries.forEachIndexed { i, (firArg, astArg) -> // Type check args
                     val astArgType = astArg.type()

                     if (!(firArg.typeAnnotation accepts astArgType)) {
                         DiagnosticPipe.report (
                             CompilerDiagnostic (
                                 level = CompilerDiagnostic.Level.Error,
                                 message = "Type mismatch for argument $i: expected '${firArg.typeAnnotation}', found '${astArgType}'",
                                 astNode = astExpr
                             )
                         )
                     }
                 }

                 resolvedFunction.returnType
             }
             is CyanStructLiteralExpression -> {
                 val containingDecl = firstAncestorOfType<FirVariableDeclaration>()
                     ?: DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "struct literal must be part of a variable initialization",
                             astNode = astExpr
                         )
                     )

                 val type = containingDecl.typeAnnotation
                     ?: DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "variable initialized with a struct literal must have a type annotation",
                             astNode = astExpr
                         )
                     )

                 if (type !is Type.Struct) { // Check annotation refers to a struct
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "Type mismatch: expected 'any struct', found '$type'",
                             astNode = astExpr
                         )
                     )
                 }

                 when {
                     astExpr.exprs.size > type.properties.size -> { // Check for too many arguments
                         DiagnosticPipe.report (
                             CompilerDiagnostic (
                                 level = CompilerDiagnostic.Level.Error,
                                 message = "Too many arguments for struct literal of type '${type.name}'",
                                 astNode = astExpr,
                                 note = CompilerDiagnostic.Note("type '${type.name}' is defined as follows\n" + "    | ".lightGray() + "$type")
                             )
                         )
                     }
                     type.properties.size > astExpr.exprs.size -> { // Check for not enough arguments
                         DiagnosticPipe.report (
                             CompilerDiagnostic (
                                 level = CompilerDiagnostic.Level.Error,
                                 message = "Not enough arguments for struct literal of type '${type.name}'",
                                 astNode = astExpr,
                                 note = CompilerDiagnostic.Note("type '${type.name}' is defined as follows\n" + "    | ".lightGray() + "$type")
                             )
                         )
                     }
                     else -> for ((index, field) in type.properties.withIndex()) { // Check for argument type mismatch
                         val exprFieldValue = FirExpression(this, astExpr.exprs[index])

                         if (!(field.type accepts exprFieldValue.type())) {
                             DiagnosticPipe.report (
                                 CompilerDiagnostic (
                                     level = CompilerDiagnostic.Level.Error,
                                     message = "Type mismatch for argument $index (${field.name}): expected '${field.type}', found '${exprFieldValue.type()}'",
                                     astNode = astExpr,
                                     note = CompilerDiagnostic.Note("type '${type.name}' is defined as follows\n" + "    | ".lightGray() + "$type")
                                 )
                             )
                         }
                     }
                 }

                 type
             }
             is CyanArrayExpression -> FirExpression(this, astExpr.exprs.first()).type().asArrayType()
             is CyanBinaryExpression -> {
                 val (lhsType, rhsType) = FirExpression(this, astExpr.lhs).type() to FirExpression(this, astExpr.rhs).type()

                 if (lhsType != rhsType) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "binary expressions with different operand types are not yet supported",
                             astNode = astExpr
                         )
                     )
                 }

                 if (astExpr.operator is CyanBinaryComparisonOperator) {
                     Type.Primitive(CyanType.Bool, false)
                 } else lhsType
             }
             is CyanIdentifierExpression -> {
                 val containingScope = this.containingScope()

                 when (val referee = containingScope?.findSymbol(FirReference(this, this.astExpr.value))) {
                     is FirVariableDeclaration -> referee.initializationExpr.type()
                     is FirFunctionDeclaration -> Type.Primitive(CyanType.Any, false)
                     is FirFunctionArgument -> referee.typeAnnotation
                     null -> error("cannot find symbol '${astExpr.value}'")
                     else -> error("can't infer type of ${referee::class.simpleName}")
                 }
             }
             is CyanMemberAccessExpression -> {
                 val baseType = FirExpression(this, astExpr.base).type()
                 val memberName = astExpr.member.value

                 when (baseType) {
                     is Type.Primitive -> DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "Primitives do not have members",
                             astNode = astExpr
                         )
                     )
                     is Type.Struct -> {
                         val matchingStructProperty = baseType.properties.firstOrNull { it.name == memberName }
                             ?: DiagnosticPipe.report (
                                 CompilerDiagnostic (
                                     level = CompilerDiagnostic.Level.Error,
                                     message = "Type '${baseType.name}' does not have a member called '$memberName'",
                                     astNode = astExpr,
                                     note = CompilerDiagnostic.Note("type '${baseType.name}' is defined as follows\n" + "    | ".lightGray() + "$baseType")
                                 )
                             )

                         matchingStructProperty.type
                     }
                 }
             }
             is CyanArrayIndexExpression -> {
                 val baseExprType = FirExpression(this, astExpr.base).type()

                 if (!baseExprType.array) {
                     DiagnosticPipe.report (
                         CompilerDiagnostic (
                             level = CompilerDiagnostic.Level.Error,
                             message = "left-hand side of array index expression must refer to an array",
                             astNode = astExpr
                         )
                     )
                 }

                 when (baseExprType) {
                     is Type.Primitive -> Type.Primitive(baseExprType.base, false)
                     is Type.Struct    -> Type.Struct(baseExprType.name, baseExprType.properties, false)
                 }
             }
             else -> error("can't infer type of ${astExpr::class.simpleName}")
        }
    }

    override fun allReferredSymbols(): Set<FirSymbol> = emptySet()

}
