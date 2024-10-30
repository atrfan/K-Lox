package tools

import java.io.PrintWriter
import java.util.*


object GenerateAst {
    @JvmStatic
    fun main(args: Array<String>) {
//        if (args.size != 1) {
//            System.err.println("Usage: generate_ast <output directory>")
//            System.exit(64)
//        }
//        val outputDir = args[0]
        val outputDir = "D:\\IDEA Project\\K-Lox\\src\\main\\kotlin\\language"
        defineAst(
            outputDir, "Expr", listOf(
                "Assign   ->  name:Token,value:Expr",       // 变量赋值
                "Binary   ->  left:Expr,operator:Token, right:Expr",
                "Grouping ->  expression:Expr",
                "Literal  ->  value:Any?",
                "Logical  ->  left:Expr,operator: Token,right:Expr",
                "Unary    ->  operator:Token,  right:Expr",
                "Variable -> name:Token"
            )
        )
        defineAst(
            // Stmt: Statement,语句
            outputDir, "Stmt", listOf(
                "Block      -> statements:List<Stmt?>",
                "Expression      ->  expression:Expr",
                "If         -> condition:Expr, thenBranch:Stmt, elseBranch:Stmt?    ",
                "Print      ->  expression:Expr",
                "Var        ->  name:Token, initializer:Expr",
                "While      -> condition:Expr, body:Stmt"
            )
        )

    }

    private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir\\$baseName.kt"
        println(path)
        val writer = PrintWriter(path, "UTF-8")
        writer.println("package language;")
        writer.println()
        writer.println("abstract class $baseName {")
        defineVisitor(writer, baseName, types)
        for (type in types) {
            val (className, field) = type.split("->").map { it.trim { it <= ' ' } }
            defineType(writer, baseName, className, field)
        }
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R?")
        writer.println("}")
        writer.close()
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<R> {")
        for (type in types) {
            val (typeName, _) = type.split("->").map { it.trim { it <= ' ' } }
            writer.println("        fun visit${typeName}${baseName}(${baseName.lowercase(Locale.getDefault())}:${typeName}): R?")
        }
        writer.println("    }")
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, field: String) {
        writer.printf("    class $className(")
        writer.printf(field.split(",").joinToString(", ") { "val ${it.trim()}" })


        writer.println(") :${baseName}(){")
        writer.println("        override fun <R> accept(visitor: Visitor<R>): R? {")
        writer.println("            return visitor.visit${className}${baseName}(this)")
        writer.println("        }")
        writer.println("    }")
    }
}