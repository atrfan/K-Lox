import language.Lox
import kotlin.test.Test

class IfTest {
    @Test
    fun test() {
        val sources = "var a = 10;\n" +
                "if(a < 10){\n" +
                "    print 10;\n" +
                "} else {\n" +
                "    print a+10;\n" +
                "}"
        Lox.run(sources)
    }
}