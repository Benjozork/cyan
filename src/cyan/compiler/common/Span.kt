package cyan.compiler.common

import com.github.h0tk3y.betterParse.lexer.TokenMatch

class Span(val line: Int, val position: IntRange, val fromTokenMatches: Array<TokenMatch>)
