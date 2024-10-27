package language

/**
 * 将tokens解析为expression
 * 规则如下
expression     → equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
 */
class Parser(private val tokens: List<Token>) {

    private var current = 0

    // 这是一个简单的哨兵类，我们用它来帮助解析器摆脱错误。
    class ParseError : RuntimeException()

    fun parse():List<Stmt>{
        val statements:MutableList<Stmt> = mutableListOf()
        while (!isAtEnd()){
            statements.add(statement())
        }
        return statements
    }


    private fun expression(): Expr {
        return equality()
    }

    /**
     * 我们通过查看当前标记来确定匹配哪条语句规则。`print`标记意味着它显然是一个`print`语句。
     * @return Stmt
     */
    private fun statement(): Stmt {
        if(match(TokenType.PRINT)){
            return printStatement()
        }
        return exceptionStatement()
    }

    private fun printStatement(): Stmt {
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun exceptionStatement(): Stmt{
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }


    private fun equality(): Expr {
        var expr = comparison()

        // 在规则体中，我们必须先找到一个 != 或 == 标记
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }


    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }


    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    /**
     * 获取current所指向的type
     * @return Token
     */
    private fun peek(): Token {
        return tokens[current]
    }

    /**
     * 返回上一个消费的标记
     * @return Token
     */
    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun comparison(): Expr {
        var expr: Expr = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right: Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * 加减法
     */
    private fun term(): Expr {
        var expr: Expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right: Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * 乘除法
     * @return Expr
     */
    private fun factor(): Expr {
        var expr: Expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right: Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    /**
     * 取反或者取负
     * @return Expr
     */
    private fun unary(): Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(TokenType.FALSE)) return Expr.Literal(false)
        if (match(TokenType.TRUE)) return Expr.Literal(true)
        if (match(TokenType.NIL)) return Expr.Literal(null)

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    /**
     * 判断当前的标记是否属于给定的类型之一。如果是，则消费该标记并返回`true`；否则，就返回`false`并保留当前标记
     * @param types Array<out TokenType>
     * @return Boolean
     */
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }


    /**
     * 消费当前词法分析器位置的令牌
     *
     * 此函数尝试消耗当前的令牌，如果当前令牌的类型与预期的类型匹配，则移动到下一个令牌
     * 否则，将抛出一个错误，指示在解析过程中遇到了意外的令牌
     *
     * @param type 预期当前令牌应匹配的类型
     * @param message 当遇到的令牌类型不匹配时，抛出的错误消息
     * @return 如果当前令牌类型匹配，则返回下一个令牌；否则抛出错误
     */
    private fun consume(type: TokenType, message: String): Token? {
        // 检查当前令牌是否为预期类型，如果是，则移动到下一个令牌，并返回该令牌
        if (check(type)) return advance()

        // 如果当前令牌类型不符合预期，则抛出错误，指示解析过程中的问题
        throw error(peek(), message)
    }

    /**
     * 报告错误,他的作用是报告错误而不是抛出错误
     * @param token Token
     * @param message String
     * @return ParseError
     */
    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }


    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR, TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
                else -> {}
            }
            advance()
        }
    }
}