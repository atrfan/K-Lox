package language

import language.Lox.error


class Scanner(val source: String) {
    val tokens = ArrayList<Token>()
    var start = 0       // 当前被扫描词素中的第一个字符
    var current = 0     // 当前被扫描的字符
    var line = 1        // current所在的源文件的第几行

    companion object{
        val keywords = HashMap<String, TokenType>().apply {
            put("and", TokenType.AND);
            put("class", TokenType.CLASS);
            put("else", TokenType.ELSE);
            put("false", TokenType.FALSE);
            put("for", TokenType.FOR);
            put("fun", TokenType.FUN);
            put("if", TokenType.IF);
            put("nil", TokenType.NIL);
            put("or", TokenType.OR);
            put("print", TokenType.PRINT);
            put("return", TokenType.RETURN);
            put("super", TokenType.SUPER);
            put("this", TokenType.THIS);
            put("true", TokenType.TRUE);
            put("var", TokenType.VAR);
            put("while", TokenType.WHILE);
        }
    }

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current;
            scanTokens()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    fun scanToken() {
        val c = advance()
        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS);
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)

            // 当前字符为'!'，查看下一个是否为=，这样就可以判断这个词素是取‘非’还是取‘不等于’的意思了
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    // 连着两个‘/’，表示为注释
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
            }

            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '"' -> string()
            'o' -> if(peek() == 'r') {addToken(TokenType.OR)}
            else -> if(isDigit(c)){
                number()
            } else if(isAlpha(c)){
                identifier()
            } else {
                Lox.error(line, "Unexpected character: ${c}")
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }
        val text = source.substring(start, current)
        val tokenType = keywords[text]?: TokenType.IDENTIFIER
        addToken(TokenType.IDENTIFIER)
    }

    private fun isAlpha(c: Char): Boolean {
        return (c in 'a'..'z') ||
                (c in 'A'..'Z') || (
                c == '_')
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    /**
     * 将current指针向前移动一个后，返回当前指针指向的字符
     * @return Char
     */
    private fun advance(): Char {
        current++
        return source[current - 1]
    }


    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    /**
     * 是否已经读取到了文件的末尾
     * @return Boolean
     */
    private fun isAtEnd() = current >= source.length

    /**
     * 查看当前扫描的字符的下一个字符是否==excepted
     * @param expected Char
     * @return Boolean
     */
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    /**
     * 返回下一个字符
     * @return Char
     */
    private fun peek() = if (isAtEnd()) '\u0000' else source[current]

    private fun peekNext( ) = if(current + 1 >= source.length) '\u0000' else source[current + 1]


    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // 吞掉右边的那个“
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun isDigit(c: Char) = c in '0'..'9'

    private fun number() {
        while(isDigit(peek())) advance()
        if(peek() == '.' && isDigit(peekNext())) {
            advance()
            while(isDigit(peek())) advance()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }
}