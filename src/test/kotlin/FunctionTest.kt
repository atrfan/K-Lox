import language.Lox
import kotlin.test.Test

class FunctionTest {

    @Test
    fun noReturn() {
        val sources = "fun sayHi(first, last) {\n" +
                "  print \"Hi, \" + first + \" \" + last + \"!\";\n" +
                "}\n" +
                "\n" +
                "sayHi(\"Dear\", \"Reader\");"
        Lox.run(sources)
    }

    @Test
    fun returnValue1() {
        val sources = "fun procedure() {\n" +
                "  print \"don't return anything\";\n" +
                "}\n" +
                "\n" +
                "var result = procedure();\n" +
                "print result; // ?"
        Lox.run(sources)
    }

    @Test
    fun returnValue2() {
        val sources = "fun count(n) {\n" +
                "  while (n < 100) {\n" +
                "    if (n == 3) return n; // <--\n" +
                "    print n;\n" +
                "    n = n + 1;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "count(1);"
        Lox.run(sources)
    }

    @Test
    fun closureTest() {
        val sources = "fun makeCounter() {\n" +
                "  var i = 0;\n" +
                "  fun count() {\n" +
                "    i = i + 1;\n" +
                "    print i;\n" +
                "  }\n" +
                "\n" +
                "  return count;\n" +
                "}\n" +
                "\n" +
                "var counter = makeCounter();\n" +
                "counter(); // \"1\".\n" +
                "counter(); // \"2\"."
        Lox.run(sources)

    }

}