import language.Parser
import language.Scanner
import language.Token
import language.TokenType
import org.junit.jupiter.api.Test

class ParserTest {
    @Test
    fun testParse() {
        val testString = "!(data >= 10)"
        val scanner = Scanner(testString)
        val parser = Parser(scanner.scanTokens())
        parser.parse()
    }
}