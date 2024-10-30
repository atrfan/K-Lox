import language.Lox
import kotlin.test.Test

class LoopTest {
    @Test
    fun test(){
        val string = "var a = 0;\n" +
                "var temp;\n" +
                "\n" +
                "for (var b = 1; a < 10000; b = temp + b) {\n" +
                "  print a;\n" +
                "  temp = a;\n" +
                "  a = b;\n" +
                "}"
        Lox.run(string)
    }
}