import language.Lox
import kotlin.test.Test

class LoopTest {

    @Test
    fun whileTest() {
        val string = "var a = 10;\n" +
                "while(a < 20){\n" +
                "    print a;\n" +
                "    a = a + 1;\n" +
                "}"
        Lox.run(string)
    }

    @Test
    fun test() {
        val string = "var a = 0;\n" +
                "var temp;\n" +
                "temp = 10;" +
                "\n" +
                "for (var b = 1; a < 10000; b = temp + b) {\n" +
                "  print a;\n" +
                "  temp = a;\n" +
                "  a = b;\n" +
                "}"
        Lox.run(string)
    }
}