import language.Interpreter
import language.Parser
import language.Scanner
import org.junit.jupiter.api.Test

class InterpreterTest {
    @Test
    fun testInterpreter() {
        val interpreter = Interpreter()
        val code = """
           "abc" +"9"  
        """.trimIndent()
        val tokens = Scanner(code).scanTokens()
        val parsers = Parser(tokens).parse()
        if (parsers != null) {
            println(interpreter.interpret(parsers))
        }
//        for(expr in expressions) {
//            interpreter.interpret(expr)
//        }

    }
}