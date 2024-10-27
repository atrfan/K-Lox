import language.Scanner
import org.junit.jupiter.api.Test

class ScannerTest {
    @Test
    fun test(){
        val source = "or\n(( )){}\n123.456\"this is a string\""
        val mid = Scanner(source).scanTokens()
        println(mid)
    }
}