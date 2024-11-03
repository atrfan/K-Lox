package language

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {

    // 解释器中的`environment`字段会随着进入和退出局部作用域而改变，它会跟随当前环境。新加的`globals`字段则固定指向最外层的全局作用域。
    val globals = Environment()
    private var environment = globals

    init {
        // 定义一个自带的函数，用于获取当前时间戳，函数名为clock
        globals.define("clock", object : LoxCallable {
            override fun arity(): Int {     // 函数参数默认为0个
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any {
                return System.currentTimeMillis() / 1000.0
            }

            override fun toString(): String {
                return "<native fn>"
            }
        })
    }

    /**
     * 连接到解释器，然后解释表达式，最后返回结果
     */
    fun interpret(statements: List<Stmt?>) {
        try {
            for (statement in statements) {
                execute(statement)
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
     * 计算 逻辑表达式 结果
     * 我们先计算左操作数。然后我们查看结果值，判断是否可以短路。当且仅当不能短路时，我们才计算右侧的操作数。
     * @param expr Variable
     * @return Any?
     */
    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)
        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left
        } else{
            if(!isTruthy(left)) return left
        }
        return evaluate(expr.right)
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

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }

        if(callee !is LoxCallable){     // 如果被调用者无法被调用(例如“abc”()这种调用)，抛出异常
            throw RuntimeError(expr.paren,"Can only call functions and classes.")
        }
        val function = callee as LoxCallable
        if(arguments.size != function.arity()){
            throw RuntimeError(expr.paren,"Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    /**
     * 将表达式发送回解释器的访问者实现中
     * @param expr Expr
     * @return Any?
     */
    private fun evaluate(expr: Expr?): Any? {
        return expr?.accept(this)
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
        if ((left is Double) && (right is Double)) return
        throw RuntimeError(operator, "Operand must be a number.")
    }

    override fun visitBlockStmt(stmt: Stmt.Block){
        executeBlock(stmt.statements,Environment(environment))
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment){
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

    override fun visitFunctionStmt(stmt: Stmt.Function): Unit? {
        val function = LoxFunction(stmt,environment)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val evaluate = evaluate(stmt.expression)
        println(stringify(evaluate))
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Unit? {
        val value = if(stmt.value != null) evaluate(stmt.value) else null
        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value= evaluate(stmt.initializer)
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.body)
        }
    }


}