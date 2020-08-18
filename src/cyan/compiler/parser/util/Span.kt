package cyan.compiler.parser.util

import com.github.h0tk3y.betterParse.lexer.TokenMatch

import cyan.compiler.common.Span
import cyan.compiler.parser.ast.CyanItem
import cyan.compiler.parser.ast.expression.CyanExpression

fun span(begin: TokenMatch, end: TokenMatch) = Span(begin.row, begin.column..(end.column + end.length), arrayOf(begin, end))

fun span(whole: TokenMatch) = Span(whole.row, whole.column..(whole.column + whole.length), arrayOf(whole))

fun span(tokenMatchList: Iterable<TokenMatch>) = span(tokenMatchList.first(), tokenMatchList.last())

fun span(expression: CyanExpression) = span(expression.span!!.fromTokenMatches.first(), expression.span!!.fromTokenMatches.last())

fun span(firstExpression: CyanItem, secondExpression: CyanItem) =
        span(firstExpression.span!!.fromTokenMatches.first(), secondExpression.span!!.fromTokenMatches.last())

fun span(firstExpression: CyanItem, secondTokenMatch: TokenMatch) =
        span(firstExpression.span!!.fromTokenMatches.first(), secondTokenMatch)

fun span(firstExpression: TokenMatch, secondTokenMatch: CyanItem) =
        span(firstExpression, secondTokenMatch.span!!.fromTokenMatches.first())

fun span(firstSpan: Span, secondSpan: Span) = span(firstSpan.fromTokenMatches.first(), secondSpan.fromTokenMatches.last())

fun span(begin: TokenMatch, secondSpan: Span) = span(begin, secondSpan.fromTokenMatches.last())

fun span(firstSpan: Span, end: TokenMatch) = span(firstSpan.fromTokenMatches.last(), end)
