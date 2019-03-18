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
                        classWriterProducer : KotlinClassWriterProducer,
                        private val classType : String = "open class") : Closeable {

    private val writer : BufferedWriter = classWriterProducer.classWriter(this.typeName.absoluteName)

    private val classDetails = mutableListOf<ClassDetail>()
    private val fileMethods = mutableListOf<String>()

    internal class ClassDetail(val typeName : DslTypeName,
                               val classType : String = "open class") {
        val typeImports = mutableSetOf<DslTypeName>()
        val typeAnnotations = mutableSetOf<String>()
        var typeDocumentation = ""

        var interfaceType = ""

        val typeConstructorParameters = mutableListOf<String>()
        val typeAttributeDeclarations = mutableListOf<String>()
        val typeMethodDeclarations = mutableListOf<String>()
    }

    private lateinit var current : ClassDetail

    init {
        this.push(this.typeName, this.classType)
    }

    fun push(typeName : DslTypeName, classType : String = "open class") {
        current = ClassDetail(typeName, classType)
        classDetails.add(current)
    }

    fun typeImport(importType : String) {
        this.typeImport(DslTypeName(importType))
    }

    fun typeImport(importType : DslTypeName) {
        this.classDetails.first().typeImports.add(importType)
    }

    fun typeAnnotation(annotationType : String, annotationValue : String = "") {
        this.typeAnnotation(DslTypeName(annotationType), annotationValue)
    }

    fun typeAnnotation(annotationType : DslTypeName, annotationValue : String = "") {
        this.typeImport(annotationType)
        if (annotationValue != "") {
            this.current.typeAnnotations.add(annotationValue)
        } else {
            this.current.typeAnnotations.add("@" + annotationType.typeShortName())
        }
    }

    fun typeDocumentation(documentation : String) {
        this.current.typeDocumentation = this.writeDoc(documentation)
    }

    private fun writeLine(lineContents : String) {
        this.writer.write(lineContents)
        this.writer.newLine()
    }

    fun kotlinSafe(name : String) : String {
        return if (name == "object" || name == "continue" || name.startsWith('$'))  {
            "`$name`"
        } else {
            name
        }
    }

    private fun typeConstructorParameter(modifiers : List<String>,
                                         attributeName : String,
                                         typeDeclaration : String,
                                         defaultValue : String,
                                         nullable : Boolean = false) {
        this.current.typeConstructorParameters.add(
            declaration(
                modifiers,
                attributeName,
                typeDeclaration,
                defaultValue,
                "",
                nullable
            )
        )
    }

    fun typeConstructorParameter(modifiers : List<String>,
                                 attributeName : String,
                                 declarationType : DslTypeName,
                                 defaultValue : String = "",
                                 nullable : Boolean = false,
                                 declarationTypeOverride : String ? = null) {
        this.typeImport(declarationType)

        this.typeConstructorParameter(
            modifiers,
            attributeName,
            declarationTypeOverride ?: declarationType.typeShortName(),
            defaultValue,
            nullable
        )
    }

    fun typeInterface(interfaceDeclaration : String,
                      declarationTypes : List<String>) {
        declarationTypes.forEach(this::typeImport)

        this.current.interfaceType = interfaceDeclaration
    }

    private fun declaration(modifiers : List<String>,
                            attributeName : String,
                            typeDeclaration : String,
                            defaultValue : String,
                            documentation : String,
                            nullable : Boolean) : String {
        val modifierOutput = modifiers.joinToString(" ")

        val declarationWriter = StringWriter()
        val buffered = BufferedWriter(declarationWriter)

        buffered.write(this.writeDoc(documentation, "    "))
        buffered.newLine()

        buffered.append("    ")
        buffered.append(modifierOutput)
        buffered.append(" ")
        buffered.append(this.kotlinSafe(attributeName))
        buffered.append(" : ")
        buffered.append(typeDeclaration)
        if (nullable) {
            buffered.append("?")
        }

        if (defaultValue != "") {
            buffered.append(" = ")
            buffered.append(defaultValue)
        }

        buffered.close()

        return declarationWriter.toString()
    }

    private fun typeAttribute(modifiers : List<String>,
                              attributeName : String,
                              typeDeclaration : String,
                              defaultValue : String,
                              documentation : String,
                              nullable : Boolean) {
        this.current.typeAttributeDeclarations.add(
            declaration(
                modifiers,
                attributeName,
                typeDeclaration,
                defaultValue,
                documentation,
                nullable
            )
        )
    }

    fun typeAttribute(modifiers : List<String>,
                      attributeName : String,
                      declarationType : DslTypeName,
                      defaultValue : String = "",
                      documentation : String = "",
                      nullable : Boolean = false) {
        this.typeImport(declarationType)

        this.typeAttribute(
            modifiers,
            attributeName,
            declarationType.typeShortName(),
            defaultValue,
            documentation,
            nullable
        )
    }

    fun listTypeAttribute(modifiers : List<String>,
                          attributeName : String,
                          declarationType : DslTypeName,
                          declarationItemType : DslTypeName,
                          defaultValue : String,
                          documentation : String = "",
                          nullable : Boolean = false) {
        this.typeImport(declarationType)
        this.typeImport(declarationItemType)

        val listType = declarationType.typeShortName()
        val listItemType = declarationItemType.typeShortName()

        this.typeAttribute(
            modifiers,
            attributeName,
            "$listType<$listItemType>",
            defaultValue,
            documentation,
            nullable
        )
    }

    fun mapTypeAttribute(modifiers : List<String>,
                         attributeName : String,
                         declarationType : DslTypeName,
                         declarationKeyType : DslTypeName,
                         declarationItemType : DslTypeName,
                         defaultValue : String,
                         documentation : String = "",
                         nullable : Boolean = false) {
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
            defaultValue,
            documentation,
            nullable
        )
    }

    private fun method(targetMethodLines : MutableList<String>,
                       modifiers : List<String> = emptyList(),
                       methodName : String,
                       methodParameters : List<Pair<String, String>> = emptyList(),
                       methodReturnType : String = "",
                       methodCode : List<String>,
                       methodTypeDependencies : List<String> = emptyList(),
                       methodDocumentation : String = "",
                       linePrefix : String) {
        methodTypeDependencies.forEach(this::typeImport)


        val methodWriter = StringWriter()
        val buffered = BufferedWriter(methodWriter)

        buffered.write(this.writeDoc(methodDocumentation, linePrefix))
        buffered.newLine()
        buffered.write(linePrefix)
        if (modifiers.isNotEmpty()) {
            buffered.write(modifiers.joinToString(" "))
            buffered.write(" ")
        }
        buffered.write("fun ")
        buffered.write(this.kotlinSafe(methodName))
        buffered.write("(")
        if (methodParameters.isNotEmpty()) {

            val paramIt = methodParameters.iterator()

            while (paramIt.hasNext()) {
                val paramInfo = paramIt.next()

                buffered.write(this.kotlinSafe(paramInfo.first))
                buffered.write(" : ")
                buffered.write(paramInfo.second)


                if (paramIt.hasNext()) {
                    buffered.write(", ")
                }
            }
        }
        buffered.write(") ")
        if (methodReturnType != "") {
            buffered.write(": $methodReturnType ")
        }
        buffered.write("{")
        buffered.newLine()

        methodCode.forEach {
            buffered.write("$linePrefix    $it")
            buffered.newLine()
        }

        buffered.write("$linePrefix}")
        buffered.newLine()

        buffered.close()

        targetMethodLines.add(methodWriter.toString())
    }

    fun typeMethod(modifiers : List<String> = emptyList(),
                   methodName : String,
                   methodParameters : List<Pair<String, String>> = emptyList(),
                   methodReturnType : String = "",
                   methodCode : List<String>,
                   methodTypeDependencies : List<String> = emptyList(),
                   methodDocumentation : String = "") {
        this.method(
            this.current.typeMethodDeclarations,
            modifiers,
            methodName,
            methodParameters,
            methodReturnType,
            methodCode,
            methodTypeDependencies,
            methodDocumentation,
            "    "
        )
    }

    fun fileMethod(modifiers : List<String> = emptyList(),
                   methodName : String,
                   methodParameters : List<Pair<String, String>> = emptyList(),
                   methodReturnType : String = "",
                   methodCode : List<String>,
                   methodTypeDependencies : List<String> = emptyList(),
                   methodDocumentation : String = "") {
        this.method(
            this.fileMethods,
            modifiers,
            methodName,
            methodParameters,
            methodReturnType,
            methodCode,
            methodTypeDependencies,
            methodDocumentation,
            ""
        )
    }

    override fun close() {
        val classDetailsIterator = this.classDetails.iterator()

        var first = true
        while (classDetailsIterator.hasNext()) {
            val classDetail = classDetailsIterator.next()

            if (first) {
                val packageName = classDetail.typeName.packageName()
                if (packageName != "") {
                    this.writeLine("package $packageName")

                    this.writer.newLine()
                }

                first = false
            }

            classDetail.typeImports.forEach {
                if (it.requiresImport()) {
                    this.writeLine("import ${it.absoluteName}")
                }
            }

            this.writer.newLine()

            if (classDetail.typeDocumentation != "") {
                this.writeLine(classDetail.typeDocumentation)
            }

            classDetail.typeAnnotations.forEach(this::writeLine)

            this.writer.write("${classDetail.classType} ${classDetail.typeName.typeShortName()}")

            if (classDetail.typeConstructorParameters.isNotEmpty()) {
                this.writer.write("(")
                this.writer.newLine()

                val constructorParamIterator = classDetail.typeConstructorParameters.iterator()

                while (constructorParamIterator.hasNext()) {
                    val nextConstructorParameter = constructorParamIterator.next()

                    this.writer.write(nextConstructorParameter)

                    if (constructorParamIterator.hasNext()) {
                        this.writer.write(",")
                        this.writer.newLine()
                    }
                }

                this.writer.write(")")
            }

            if (classDetail.interfaceType != "") {
                this.writer.write(" : ${classDetail.interfaceType}")
            }

            if (classDetail.typeAttributeDeclarations.isNotEmpty() || classDetail.typeMethodDeclarations.isNotEmpty()) {
                this.writer.write(" {")
                this.writer.newLine()

                this.writer.newLine()

                classDetail.typeAttributeDeclarations.forEach(this::writeLine)
                this.writer.newLine()

                classDetail.typeMethodDeclarations.forEach {
                    this.writeLine(it)
                }
                this.writer.newLine()

                this.writeLine("}")
            } else {
                this.writer.newLine()
            }

        }

        if (this.fileMethods.isNotEmpty()) {
            this.writer.newLine()

            this.fileMethods.forEach {
                this.writeLine(it)
            }
        }

        this.writer.flush()
        this.writer.close()
    }

    private fun writeDoc(documentation : String, prefix : String = "") : String {
        val docWriter = StringWriter()
        val buffered = BufferedWriter(docWriter)

        if (documentation != "") {
            buffered.write(prefix)
            buffered.write("/**")
            buffered.newLine()

            val docSplitsIt = documentation.split(" ").iterator()

            val lineLength = 120
            var lineBuffer = StringBuffer()
            lineBuffer.append(prefix)
            lineBuffer.append(" *")

            while (docSplitsIt.hasNext()) {
                val originalDocSplit = docSplitsIt.next()
                val currentDocSplit = if (originalDocSplit.startsWith("http")){
                    "[$originalDocSplit]($originalDocSplit)"
                } else {
                    originalDocSplit.replace("*", "{@literal *}")
                }

                if (lineBuffer.length + 1 + currentDocSplit.length > lineLength) {
                    // current doc split does not fit on current line
                    buffered.write(lineBuffer.toString())
                    buffered.newLine()

                    lineBuffer = StringBuffer()
                    lineBuffer.append(prefix)
                    lineBuffer.append(" *")
                }

                lineBuffer.append(" ")
                lineBuffer.append(currentDocSplit)

            }

            buffered.write(lineBuffer.toString())
            buffered.newLine()

            buffered.write(prefix)
            buffered.write(" **/")
        }

        buffered.close()
        return docWriter.toString()
    }
}