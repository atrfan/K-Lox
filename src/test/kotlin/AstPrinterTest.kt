import language.AstPrinter
import language.Expr
import language.Token
import language.TokenType
import org.junit.jupiter.api.Test


class AstPrinterTest {
    @Test
    fun testPrint() {
        val expression: Expr = Expr.Binary(
            Expr.Unary(
                Token(TokenType.MINUS, "-", null, 1),
                Expr.Literal(123)
            ),
            Token(TokenType.STAR, "*", null, 1),
            Expr.Grouping(
                Expr.Literal(45.67)
            )
        )

        println(AstPrinter().print(expression))
    }
}