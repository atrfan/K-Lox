package language

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


/**
 * 入口类
 * 有两种方式执行lox语言
 * 1.命令行执行脚本文件：k-lox xx.lox
 * 2.交互式的启动： 会有一个提示符，你可以在提示符处一次输入并执行一行代码
 */
object Lox {
    @JvmStatic
    var hadError = false

    @JvmStatic
    var hadRuntimeError = false

    @JvmStatic
    private val interpreter = Interpreter()

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            System.err.println("Usage: k-lox [script]")
            System.exit(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }


    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }


    /**
     * 每次读取一行代码并执行他
     * */
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            // 在交互式循环中重置此标志。 如果用户输入有误，也不应终止整个会话。
            hadError = false
        }
    }


    /**
     * 对于输入的代码 sources，把他进行词法分析、语法分析，得到表达式，然后执行
     * @param source String
     */
    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        val parser = Parser(tokens)
        val statements = parser.parse()


        // 如果有错误就不执行他
        if (hadError) return
        statements.let { interpreter.interpret(it) }
    }


    // 错误处理

    fun error(line: Int, message: String) {
        report(line, "", message)
    }


    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message]")
    }

    fun error(token: Token, message: String?) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message!!)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message!!)
        }
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println(
            error.message +
                    "\n[line " + error.token.line + "]"
        )
        hadRuntimeError = true;
    }


}