package language

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Lox {
    @JvmStatic
    var hasError = false

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: jlox [script]")
            System.exit(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    @JvmStatic
    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if(hasError) System.exit(65)
    }

    @JvmStatic
    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hasError = false
        }
    }

    @JvmStatic
    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens()
        for (token in tokens) {
            println(token)
        }
    }

    @JvmStatic
    fun error(line:Int,message:String){
        report(line,"",message)
    }

    @JvmStatic
    private fun report(line:Int,where:String,message:String){
        println("[line $line] Error $where: $message]")
    }


}