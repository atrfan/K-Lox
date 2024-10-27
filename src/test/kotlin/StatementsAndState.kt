import language.Lox
import kotlin.test.Test

class StatementsAndStateTest {
    @Test
    fun statementsTest(){
        val string = "print \"one\";\n" +
                "print true;\n" +
                "print 2 + 1;"
        Lox.run(string)
    }
}