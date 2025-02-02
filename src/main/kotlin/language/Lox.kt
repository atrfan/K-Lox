package language

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


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


    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }


    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
//        for (token in tokens) {
//            println(token)
//        }
        val parser = Parser(tokens)
        val expression = parser.parse()


        // Stop if there was a syntax error.
        if (hadError) return
        expression?.let { interpreter.interpret(it) };
        println(AstPrinter().print(expression!!))
    }


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
        );
        hadRuntimeError = true;
    }


}