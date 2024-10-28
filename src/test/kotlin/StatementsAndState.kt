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

    @Test
    fun variableDeclarationTest(){
        val string = "var a = \"global a\";\n" +
                "var b = \"global b\";\n" +
                "var c = \"global c\";\n" +
                "{\n" +
                "  var a = \"outer a\";\n" +
                "  var b = \"outer b\";\n" +
                "  {\n" +
                "    var a = \"inner a\";\n" +
                "    print a;\n" +
                "    print b;\n" +
                "    print c;\n" +
                "  }\n" +
                "  print a;\n" +
                "  print b;\n" +
                "  print c;\n" +
                "}\n" +
                "print a;\n" +
                "print b;\n" +
                "print c;"
        Lox.run(string)
    }
}