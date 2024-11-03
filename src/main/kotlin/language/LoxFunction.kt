package language

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : LoxCallable {
    override fun arity(): Int {
        return declaration.params.size
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        for (i in declaration.params.indices) {
            environment.define(declaration.params[i]!!.lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    //   如果用户要打印函数的值,可以直接输出函数名称。
    //   例如定义一个函数fun add(...){...} ,运行代码'print add',可以得到输出结果"<fn add>".
    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }

}