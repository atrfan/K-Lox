package language

/**
 *
 * @property type TokenType 类型
 * @property lexeme String 词素
 * @property literal Any?   文字
 * @property line Int   所在行数
 * @constructor
 */
data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val line: Int)
