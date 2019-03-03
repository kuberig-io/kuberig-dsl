package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.*
import java.io.StringWriter

class KotlinApiTypeDslTypeGenerator(private val dslMeta : DslMeta,
                                    private val classWriterProducer : KotlinClassWriterProducer,
                                    private val attributeIgnores : List<String> = emptyList(),
                                    private val kindMeta : DslKindMeta? = null) {


    fun generateApiTypeDslType(typeName : DslTypeName, typeMeta : DslTypeMeta) {

        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)

        when(typeMeta) {
            is DslObjectTypeMeta -> this.generateObjectDsl(typeMeta, kotlinClassWriter)
            is DslContainerTypeMeta -> this.generateContainerDsl(typeMeta, kotlinClassWriter)
            is DslSealedTypeMeta -> this.generatedSealedDsl(typeMeta, kotlinClassWriter)
            is DslInterfaceTypeMeta -> this.generateInterfaceDsl(typeMeta, kotlinClassWriter)
            else -> throw IllegalStateException("Don't know what to do with " + typeMeta::javaClass)
        }
    }

    private fun generateInterfaceDsl(typeMeta: DslInterfaceTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<${typeMeta.name}>",
                listOf(
                    "eu.rigeldev.kuberig.dsl.DslType",
                    typeMeta.absoluteName
                )
            )

            classWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "toValue",
                methodReturnType = typeMeta.name,
                methodCode = listOf(
                    "return ${typeMeta.name}()"
                )
            )
        }
    }

    private fun generatedSealedDsl(typeMeta: DslSealedTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<${typeMeta.name}>",
                listOf(
                    "eu.rigeldev.kuberig.dsl.DslType",
                    typeMeta.absoluteName
                )
            )

            classWriter.typeAttribute(
                listOf("private", "var"),
                "value",
                DslTypeName(typeMeta.absoluteName),
                "null",
                nullable = true
            )

            typeMeta.sealedTypes.forEach { name, typeName ->

                classWriter.typeMethod(
                    methodName = "value",
                    methodParameters = "value : ${typeName.typeShortName()}",
                    methodCode = listOf(
                        "this.value = ${typeMeta.name}_$name(value)"
                    )
                )

            }

            classWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "toValue",
                methodReturnType = typeMeta.name,
                methodCode = listOf(
                    "return this.value!!"
                )
            )
        }
    }

    private fun generateContainerDsl(typeMeta: DslContainerTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<${typeMeta.name}>",
                listOf(
                    "eu.rigeldev.kuberig.dsl.DslType",
                    typeMeta.absoluteName
                )
            )

            classWriter.typeAttribute(
                listOf("var"),
                "value",
                typeMeta.containedType,
                "null",
                nullable = true
            )

            if (typeMeta.containedType.requiresImport()) {

                if (this.dslMeta.isPlatformApiType(typeMeta.containedType)) {
                    /*
                    writer.write("    fun ${typeMeta.name.toLowerCase()}(init : ${typeMeta.containedType.typeShortName()}.() -> Unit) : ${typeMeta.containedType.typeShortName()} {")
                    writer.write("        val attr = ${typeMeta.containedType.typeShortName()}()")
                    writer.write("        attr.init()")
                    writer.write("        this.value = attr")
                    writer.write("        return attr")
                    writer.write("    }")
                     */
                    throw IllegalStateException("really unexpected... this is")
                }

            } else {
                classWriter.typeMethod(
                    methodName = typeMeta.name.toLowerCase(),
                    methodParameters = "value : ${typeMeta.containedType.typeShortName()}",
                    methodCode = listOf(
                        "this.value = value"
                    )
                )
            }

            classWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "toValue",
                methodReturnType = typeMeta.name,
                methodCode = listOf(
                    "return ${typeMeta.name}(this.value!!)"
                )
            )
        }
    }

    private fun generateObjectDsl(typeMeta: DslObjectTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<${typeMeta.name}>",
                listOf(
                    "eu.rigeldev.kuberig.dsl.DslType",
                    typeMeta.absoluteName
                )
            )

            typeMeta.attributes.minus(attributeIgnores).forEach { attributeName, attributeMeta ->

                if (attributeMeta is DslListAttributeMeta) {
                    val listDslMeta = DslListDslMeta(DslTypeName(typeMeta.absoluteName), attributeMeta)
                    this.dslMeta.listDslTypes.add(listDslMeta)

                    classWriter.typeAttribute(
                        listOf("private", "val"),
                        attributeName,
                        listDslMeta.declarationType(),
                        listDslMeta.declarationType().typeShortName() + "()",
                        attributeMeta.description
                    )

                    classWriter.typeMethod(
                        methodDocumentation = attributeMeta.description,
                        methodName = attributeName,
                        methodParameters = "init: ${listDslMeta.declarationType().typeShortName()}.() -> Unit",
                        methodCode = listOf(
                            "this.$attributeName.init()"
                        )
                    )

                }
                else if (attributeMeta is DslMapAttributeMeta) {
                    val mapDslMeta = DslMapDslMeta(DslTypeName(typeMeta.absoluteName), attributeMeta)
                    this.dslMeta.mapDslTypes.add(mapDslMeta)

                    classWriter.typeAttribute(
                        listOf("private", "val"),
                        attributeName,
                        mapDslMeta.declarationType(),
                        mapDslMeta.declarationType().typeShortName() + "()",
                        attributeMeta.description
                    )

                    classWriter.typeMethod(
                        methodDocumentation = attributeMeta.description,
                        methodName = attributeName,
                        methodParameters = "init: ${mapDslMeta.declarationType().typeShortName()}.() -> Unit",
                        methodCode = listOf(
                            "this.$attributeName.init()"
                        )
                    )
                }
                else if (attributeMeta is DslObjectAttributeMeta) {

                    if (attributeMeta.absoluteType.requiresImport()) {

                        classWriter.typeAttribute(
                            listOf("private", "var"),
                            attributeName,
                            DslTypeName(attributeMeta.absoluteType.absoluteName + "Dsl"),
                            "null",
                            nullable = true
                        )

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = "init : ${attributeMeta.attributeDeclarationType()}Dsl.() -> Unit",
                            methodCode = listOf(
                                "val attr = ${attributeMeta.attributeDeclarationType()}Dsl()",
                                "attr.init()",
                                "this.$attributeName = attr"
                            )
                        )

                    } else {

                        classWriter.typeAttribute(
                            listOf("var"),
                            attributeName,
                            attributeMeta.absoluteType,
                            "null",
                            attributeMeta.description,
                            true
                        )

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = "$attributeName : ${attributeMeta.attributeDeclarationType()}",
                            methodCode = listOf(
                                "this.$attributeName = $attributeName"
                            )
                        )
                    }
                }
            }

            val toValueMethodCode = mutableListOf<String>()

            toValueMethodCode.add("return ${typeMeta.name}(")

            val toValueAttributeIgnores = mutableListOf<String>()
            toValueAttributeIgnores.addAll(this.attributeIgnores.minus(listOf("kind", "apiVersion")))

            val constructorIt = typeMeta.attributes.minus(toValueAttributeIgnores).iterator()
            while(constructorIt.hasNext()) {
                val (attributeName, attributeMeta) = constructorIt.next()

                val constructorParameter = StringWriter()

                constructorParameter.append("    $attributeName = ")
                constructorParameter.append(
                    when (attributeName) {
                        "kind" ->  {
                            if (this.kindMeta != null) {
                                "\"${this.kindMeta.kind}\""
                            } else {
                                "this.$attributeName${attributeMeta.toValueConstructorSuffix()}"
                            }
                        }
                        "apiVersion" -> {
                            if (this.kindMeta != null) {
                                "\"${this.kindMeta.apiVersion()}\""
                            } else {
                                "this.$attributeName${attributeMeta.toValueConstructorSuffix()}"
                            }
                        }
                        else -> "this.$attributeName${attributeMeta.toValueConstructorSuffix()}"
                    }
                )

                if (constructorIt.hasNext()) {
                    constructorParameter.append(", ")
                }

                toValueMethodCode.add(constructorParameter.toString())

            }

            toValueMethodCode.add(")")

            classWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "toValue",
                methodReturnType = typeMeta.name,
                methodCode = toValueMethodCode
            )
        }
    }

}