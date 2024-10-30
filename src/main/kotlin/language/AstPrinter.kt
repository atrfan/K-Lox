package language

/**
 * 查看解析后的语法树
 */
class AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr): String? {
        return expr.accept(this)
    }

    override fun visitAssignExpr(expr: Expr.Assign): String? {
        TODO("Not yet implemented")
    }

    override fun visitBinaryExpr(expr: Expr.Binary) = parenthesize(
        expr.operator.lexeme,
        expr.left, expr.right
    )

    override fun visitGroupingExpr(expr: Expr.Grouping) = parenthesize("group", expr.expression);

    override fun visitLiteralExpr(expr: Expr.Literal) = if (expr.value == null) "nil" else expr.value.toString()
    override fun visitLogicalExpr(expr: Expr.Logical): String? {
            TODO("Not yet implemented")
    }

    override fun visitUnaryExpr(expr: Expr.Unary) = parenthesize(expr.operator.lexeme, expr.right)
    override fun visitVariableExpr(expr: Expr.Variable): String? {
        return ""
    }

    private fun parenthesize(name: String, vararg exprs: Expr) =
        StringBuilder().apply {
            append("(")
            append(name)
            for (expr in exprs) {
                append(" ")
                append(expr.accept(this@AstPrinter))
            }
            append(")")
        }.toString()
}