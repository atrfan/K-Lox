package language

import language.Stmt.While

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

    fun parse(): List<Stmt?> {
        val statements: MutableList<Stmt?> = mutableListOf()
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        return statements
    }

    private fun declaration(): Stmt? {
        try {
            if (match(TokenType.VAR)) return varDeclaration()
            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun varDeclaration(): Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr? = null
        if (match(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name!!, initializer!!)
    }


    private fun expression(): Expr {
        return assignment()
    }

    private fun assignment(): Expr {
        val expr = or()
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            throw error(equals, "Invalid assignment target.")

        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()
        while(match(TokenType.OR)){
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()
        while(match(TokenType.AND)){
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        return expr
    }

    /**
     * 我们通过查看当前标记来确定匹配哪条语句规则。`print`标记意味着它显然是一个`print`语句。
     * @return Stmt
     */
    private fun statement(): Stmt {
        if(match(TokenType.FOR)){
            return forStatement()
        }

        if(match(TokenType.IF)){
            return ifStatement()
        }

        if (match(TokenType.PRINT)) {
            return printStatement()
        }
        
        if(match(TokenType.WHILE)){
            return whileStatement()
        }

        if(match(TokenType.LEFT_BRACE)){        // 通过块的前缀标记(在本例中是`{`)来检测块的开始
            return Stmt.Block(block())
        }

        return expressionStatement()
    }

    private fun forStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        var initializer:Stmt? = null        // for 循环的第一个项，变量初始化
        // 有三种情况：1.检测到的是';",表示空；2.变量声明；3.表达式
        if(match(TokenType.SEMICOLON)){
            initializer = null
        } else if (match(TokenType.VAR)){
            initializer = varDeclaration()
        } else {
            initializer = expressionStatement()
        }

        // 判断部分
        var condition:Expr? = null
        if(!check(TokenType.SEMICOLON)){
            condition = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        var increment:Expr? = null
        if(!check(TokenType.RIGHT_PAREN)){
            increment = expression()
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after increment.")

        var body = statement()
        if(increment != null){
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }
        if(condition == null){  // 条件为空，默认为true
            condition = Expr.Literal(true)
        }
        body = Stmt.While(condition, body)
        if(initializer != null){        // 初始化语句不为空，则将其作为初始化语句放入块中
            body = Stmt.Block(listOf(initializer, body))
        }
        return body
    }

    private fun whileStatement(): Stmt {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return While(condition, body)
    }

    private fun ifStatement(): Stmt {
        consume(TokenType.LEFT_PAREN,"Except '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Stmt? = null
        if(match(TokenType.ELSE)){
            elseBranch = statement()
        }
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement(): Stmt {
        val value: Expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    /**
     * 块语句;
     * 我们先创建一个空列表，然后解析语句并将其放入列表中，直至遇到块的结尾（由`}`符号标识）。
     * 注意，该循环还有一个明确的`isAtEnd()`检查。我们必须小心避免无限循环，即使在解析无效代码时也是如此。如果用户忘记了结尾的`}`，解析器需要保证不能被阻塞。
     * @return List<Stmt?>
     */
    private fun block():List<Stmt?>{
        val statements = mutableListOf<Stmt?>()
        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration())
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements
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

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
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