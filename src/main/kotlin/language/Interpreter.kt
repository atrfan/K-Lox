package language

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {

    private var environment = Environment()

    /**
     * 连接到解释器，然后解释表达式，最后返回结果
     */
    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement!!)
            }
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }

    private fun execute(statement: Stmt?) {
        statement?.accept(this)
    }



    private fun stringify(any: Any?): String {
        if (any == null) return "nil"
        if (any is Double) {
            var text = any.toString()
            // Lox即使对整数值也使用双精度数字
            // 由于Java同时具有浮点型和整型，它希望您知道正在使用的是哪一种类型。它通过在整数值的双数上添加一个明确的`.0`来告知用户。我们不关心这个，所以我们把它去掉。
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2);
            }
            return text
        }
        return any.toString()
    }


    /**
     * 分组——在表达式中显式使用括号时产生的语法树节点，有的编译器不显示的使用括号来单独定义树节点
     * @param expr Grouping
     * @return Any?
     */
    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    /**
     * 字面量直接返回它的值就行
     * @param expr Literal
     * @return Any?
     */
    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    /**
     * 一元表达式
     * @param expr Unary
     * @return Any?
     */
    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }

            TokenType.BANG -> !isTruthy(right)
            else -> {
                null
            }
        }
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    /**
     * 二元表达式求职
     * @param expr Binary
     * @return Any?
     */
    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }

            TokenType.PLUS -> {
                if ((left is Double) && (right is Double)) {
                    left + right
                } else if ((left is String) && (right is String)) {
                    left + right
                } else {
                    throw RuntimeError(
                        expr.operator,
                        "Operands must be two numbers or two strings."
                    )
                }
            }

            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }

            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }

            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }

            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }

            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }

            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }

            TokenType.BANG_EQUAL -> !isEqual(left, right)
            TokenType.EQUAL -> isEqual(left, right)

            else -> null

        }
    }

    /**
     * 将表达式发送回解释器的访问者实现中
     * @param expr Expr
     * @return Any?
     */
    private fun evaluate(expr: Expr): Any? {
        return expr.accept(this)
    }

    /**
     * `false`和`nil`是假的，其他都是真的。
     */
    private fun isTruthy(any: Any?): Boolean {
        if (any == null) return false
        if (any is Boolean) return any
        return true
    }

    private fun isEqual(left: Any?, right: Any?): Boolean {
        if (left == null && right == null) return true
        if (left == null) return false
        return left == right
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if ((left is Double) && (right is Boolean)) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    override fun visitBlockStmt(stmt: Stmt.Block){
        executeBlock(stmt.statements,Environment(environment))
    }

    private fun executeBlock(statements: List<Stmt?>, environment: Environment) {
        val previous = this.environment
        try{
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }


    // 表达式不同，语句不会产生值，因此visit方法的返回类型是`Void`，而不是`Object`。

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val evaluate = evaluate(stmt.expression)
        println(stringify(evaluate))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value= evaluate(stmt.initializer)
        environment.define(stmt.name.lexeme, value)
    }


}