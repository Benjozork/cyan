package cyan.compiler.fir.expression

import cyan.compiler.fir.FirNode
import cyan.compiler.parser.ast.expression.CyanExpression

class FirExpression(val astExpr: CyanExpression) : FirNode
