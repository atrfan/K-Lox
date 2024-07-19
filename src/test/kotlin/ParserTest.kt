import language.Scanner
import language.Token
import language.TokenType
import org.junit.jupiter.api.Test

class ParserTest {
    @Test
    fun testParse() {
        val testString = "var data = 10"
        val scanner = Scanner(testString)
        println(scanner.scanTokens())
    }
}