
fun main() {
    println(decodeString("3[a2[c]]"))
}

fun decodeString(s: String): String {
    return solve(1, s)
}

var index = 0

    fun solve(repeat: Int, data: String): String {
        val stringBuilder = StringBuilder()
        var num = 0
        while (index < data.length) {
            if (data[index].isDigit()) {
                num = num * 10 + data[index].toString().toInt()
                index ++
            } else if (data[index] in 'a'..'z') {
                stringBuilder.append(data[index])
                index ++
            } else if (data[index] == '[') {
                index ++
                if (num == 0) {
                    stringBuilder.append(solve(1, data))
                } else {
                    stringBuilder.append(solve(num, data))
                    num = 0
                }
            } else {
                index ++
                return repeatStringBuilder(stringBuilder, repeat)
            }
        }
        return stringBuilder.toString()
    }

fun repeatStringBuilder(content: StringBuilder, times: Int): String {
    val result = StringBuilder()
    for (i in 0 until times) {
        result.append(content)
    }
    return result.toString()
}