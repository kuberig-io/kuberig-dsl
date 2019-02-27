package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import java.io.BufferedWriter
import java.io.Closeable
import java.io.StringWriter

/**
 * Helper to simplify generating Kotlin classes.
 *
 * Accumulates all needed imports.
 * Provides helper methods for defining:
 * - type annotations
 * - attribute declarations
 * - method declarations
 * - an implemented interface (can only be one at the moment).
 *
 * This helper does not make any assumptions on method call order for the output to be correct Kotlin code.
 */
class KotlinClassWriter(private val typeName : DslTypeName,
                        private val writer : BufferedWriter) : Closeable {

    private val typeImports = mutableSetOf<DslTypeName>()
    private val typeAnnotations = mutableSetOf<DslTypeName>()
    private val typeDocumentation = mutableListOf<String>()

    private var interfaceType = ""

    private val typeAttributeDeclarations = mutableListOf<String>()
    private val typeMethodDeclarations = mutableListOf<String>()

    fun typeImport(importType : String) {
        this.typeImport(DslTypeName(importType))
    }

    fun typeImport(importType : DslTypeName) {
        this.typeImports.add(importType)
    }

    fun typeAnnotation(annotationType : String) {
        this.typeAnnotation(DslTypeName(annotationType))
    }

    fun typeAnnotation(annotationType : DslTypeName) {
        this.typeImport(annotationType)
        this.typeAnnotations.add(annotationType)
    }

    fun writeLine(lineContents : String) {
        this.writer.write(lineContents)
        this.writer.newLine()
    }

    private fun kotlinSafe(name : String) : String {
        return if (name == "object" || name == "continue" || name.startsWith('$'))  {
            "`$name`"
        } else {
            name
        }
    }

    fun typeInterface(interfaceDeclaration : String,
                              declarationTypes : List<String>) {
        declarationTypes.forEach(this::typeImport)

        this.interfaceType = interfaceDeclaration
    }

    private fun typeAttribute(modifiers : List<String>,
                              attributeName : String,
                              typeDeclaration : String,
                              defaultValue : String) {
        val modifierOutput = modifiers.joinToString(" ")

        val declarationWriter = StringWriter()

        declarationWriter.append("    ")
        declarationWriter.append(modifierOutput)
        declarationWriter.append(" ")
        declarationWriter.append(this.kotlinSafe(attributeName))
        declarationWriter.append(" : ")
        declarationWriter.append(typeDeclaration)

        if (defaultValue != "") {
            declarationWriter.append(" = ")
            declarationWriter.append(defaultValue)
        }

        this.typeAttributeDeclarations.add(declarationWriter.toString())
    }

    fun typeAttribute(modifiers : List<String>,
                      attributeName : String,
                      declarationType : DslTypeName,
                      defaultValue : String = "") {
        this.typeImport(declarationType)

        this.typeAttribute(
            modifiers,
            attributeName,
            declarationType.typeShortName(),
            defaultValue
        )
    }

    fun listTypeAttribute(modifiers : List<String>,
                          attributeName : String,
                          declarationType : DslTypeName,
                          declarationItemType : DslTypeName,
                          defaultValue : String) {
        this.typeImport(declarationType)
        this.typeImport(declarationItemType)

        val listType = declarationType.typeShortName()
        val listItemType = declarationItemType.typeShortName()

        this.typeAttribute(
            modifiers,
            attributeName,
            "$listType<$listItemType>",
            defaultValue
        )
    }

    fun mapTypeAttribute(modifiers : List<String>,
                         attributeName : String,
                         declarationType : DslTypeName,
                         declarationKeyType : DslTypeName,
                         declarationItemType : DslTypeName,
                         defaultValue : String) {
        this.typeImport(declarationType)
        this.typeImport(declarationKeyType)
        this.typeImport(declarationItemType)

        val mapType = declarationType.typeShortName()
        val mapKeyType = declarationKeyType.typeShortName()
        val mapItemType = declarationItemType.typeShortName()

        this.typeAttribute(
            modifiers,
            attributeName,
            "$mapType<$mapKeyType, $mapItemType>",
            defaultValue
        )
    }

    fun typeMethod(modifiers : List<String> = emptyList(),
                   methodName : String,
                   methodParameters : String = "",
                   methodReturnType : String = "",
                   methodCode : List<String>,
                   methodTypeDependencies : List<String> = emptyList()) {
        methodTypeDependencies.forEach(this::typeImport)


        val methodWriter = StringWriter()
        val buffered = BufferedWriter(methodWriter)

        buffered.write("    ")
        if (modifiers.isNotEmpty()) {
            buffered.write(modifiers.joinToString(" "))
            buffered.write(" ")
        }
        buffered.write("fun ")
        buffered.write(this.kotlinSafe(methodName))
        buffered.write("(")
        if (methodParameters != "") {
            buffered.write(methodParameters)
        }
        buffered.write(") ")
        if (methodReturnType != "") {
            buffered.write(": $methodReturnType ")
        }
        buffered.write("{")
        buffered.newLine()

        methodCode.forEach {
            buffered.write("        $it")
            buffered.newLine()
        }

        buffered.write("    }")
        buffered.newLine()

        buffered.close()
        this.typeMethodDeclarations.add(methodWriter.toString())
    }

    override fun close() {
        val packageName = this.typeName.packageName()
        if (packageName != "") {
            this.writeLine("package $packageName")
        }

        this.writer.newLine()

        this.typeImports.forEach {
            if (it.requiresImport() && it.packageName() != this.typeName.packageName()) {
                this.writeLine("import ${it.absoluteName}")
            }
        }

        this.writer.newLine()

        this.typeDocumentation.forEach(this::writeLine)
        this.writer.newLine()

        this.typeAnnotations.forEach {
            this.writeLine("@${it.typeShortName()}")
        }

        this.writer.write("open class ${typeName.typeShortName()}")

        if (this.interfaceType != "") {
            this.writer.write(" : ${this.interfaceType}")
        }

        this.writer.write(" {")
        this.writer.newLine()

        this.writer.newLine()

        this.typeAttributeDeclarations.forEach{
            this.writeLine(it)

            this.writer.newLine()
        }
        this.writer.newLine()

        this.typeMethodDeclarations.forEach {
            this.writeLine(it)
        }
        this.writer.newLine()

        this.writeLine("}")

        this.writer.flush()
        this.writer.close()
    }


}