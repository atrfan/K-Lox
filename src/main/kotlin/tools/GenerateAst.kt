package tools

import java.io.PrintWriter
import java.util.*


object GenerateAst {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory>")
            System.exit(64)
        }
        val outputDir = args[0]
        defineAst(
            outputDir, "Expr", listOf(
                "Binary   ->  left:Expr,operator:Token, right:Expr",
                "Grouping ->  expression:Expr",
                "Literal  ->  value:Any?",
                "Unary    ->  operator:Token,  right:Expr"
            )
        )
    }

    private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        val path = "$outputDir/$baseName.kt"
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
        writer.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
        writer.println("}")
        writer.close()
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<R> {")
        for (type in types) {
            val (typeName, _) = type.split("->").map { it.trim { it <= ' ' } }
            writer.println("        fun visit${typeName}${baseName}(${baseName.lowercase(Locale.getDefault())}:${typeName}): R")
        }
        writer.println("    }")
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, field: String) {
        writer.printf("    class $className(")
        writer.printf(field.split(",").joinToString(", ") { "val ${it.trim()}" })


        writer.println(") :${baseName}(){")
        writer.println("        override fun <R> accept(visitor: Visitor<R>): R {")
        writer.println("            return visitor.visit${className}Expr(this)")
        writer.println("        }")
        writer.println("    }")
    }
}