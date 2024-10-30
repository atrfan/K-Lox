package language

class Environment(
    private val enclosing: Environment? = null      // 对变量作用域嵌套的支持
) {


    private val values = HashMap<String, Any?>()

    /**
     * 定义变量
     * @param name String
     * @param value Any?
     */
    fun define(name:String, value:Any?){
        values[name] = value
    }

    fun get(name:Token):Any?{
        if (values.containsKey(name.lexeme)) return values[name.lexeme]
        if(enclosing != null) return enclosing.get(name)        // 如果当前环境中没有找到变量，就在外围环境中尝试。

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    /**
     * 赋值语句； name = value
     * 赋值与定义的主要区别在于，赋值操作不允许创建新变量。
     * @param name Token
     * @param value Any?
     */
    fun assign(name: Token, value: Any?) {
        if(values.containsKey(name.lexeme)){
            values[name.lexeme] = value
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }
}