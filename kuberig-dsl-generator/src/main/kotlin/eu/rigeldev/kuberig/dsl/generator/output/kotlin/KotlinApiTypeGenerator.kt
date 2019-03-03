package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.*

class KotlinApiTypeGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    fun generateApiType(typeName : DslTypeName, typeMeta : DslTypeMeta) {

        when(typeMeta) {
            is DslObjectTypeMeta -> this.generateObjectType(typeName, typeMeta)
            is DslContainerTypeMeta -> this.generateContainerType(typeName, typeMeta)
            is DslSealedTypeMeta -> this.generatedSealedType(typeName, typeMeta)
            is DslInterfaceTypeMeta -> this.generateInterfaceType(typeName, typeMeta)
            else -> throw IllegalStateException("Don't know what to do with " + typeMeta::javaClass)
        }

    }

    private fun generateObjectType(typeName : DslTypeName, typeMeta: DslObjectTypeMeta) {
        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)

        kotlinClassWriter.use {classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")

            typeMeta.attributes.minus("status").forEach { attributeName, attributeMeta ->

                when (attributeMeta) {
                    is DslObjectAttributeMeta -> {
                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = attributeMeta.isOptional()
                        )
                    }
                    is DslListAttributeMeta -> {
                        classWriter.typeImport(attributeMeta.itemType)

                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = attributeMeta.isOptional(),
                            declarationTypeOverride = attributeMeta.attributeDeclarationType()
                        )
                    }
                    is DslMapAttributeMeta -> {
                        classWriter.typeImport(attributeMeta.itemType)

                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = attributeMeta.isOptional(),
                            declarationTypeOverride = attributeMeta.attributeDeclarationType()
                        )
                    }
                    else -> IllegalStateException("Don't know what to do with " + attributeMeta::javaClass)
                }
            }
        }

    }

    private fun generateContainerType(typeName : DslTypeName, typeMeta: DslContainerTypeMeta) {
        val serializerType = DslTypeName(
            typeName.packageName() + "." + typeMeta.name + "_Serializer"
        )

        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)
        // the container type
        kotlinClassWriter.use {classWriter ->

            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")

            classWriter.typeImport(serializerType)
            classWriter.typeAnnotation(
                "com.fasterxml.jackson.databind.annotation.JsonSerialize",
                "@JsonSerialize(using = ${serializerType.typeShortName()}::class)"
            )

            classWriter.typeConstructorParameter(
                listOf("val"),
                "value",
                typeMeta.containedType
            )
        }

        // the json serializer
        val kotlinSerializerWriter = KotlinClassWriter(serializerType, this.classWriterProducer)
        kotlinSerializerWriter.use { serializerWriter ->
            serializerWriter.typeAnnotation("javax.annotation.processing.Generated")

            serializerWriter.typeInterface(
                "JsonSerializer<${typeMeta.name}>()",
                listOf(
                    "com.fasterxml.jackson.databind.JsonSerializer"
                )
            )

            val writeMethod = this.determineJsonWriteMethod(typeMeta.containedType)

            serializerWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "serialize",
                methodParameters = "value: ${typeMeta.name}?, gen: JsonGenerator?, serializers: SerializerProvider?",
                methodCode = listOf(
                    "gen!!.$writeMethod(value?.value)"
                ),
                methodTypeDependencies = listOf(
                    "com.fasterxml.jackson.core.JsonGenerator",
                    "com.fasterxml.jackson.databind.SerializerProvider"
                )
            )

        }
    }

    private fun generatedSealedType(typeName : DslTypeName, typeMeta: DslSealedTypeMeta) {

        val serializerType = DslTypeName(
            typeName.packageName() + "." + typeMeta.name + "_Serializer"
        )

        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer, "sealed class")
        // the sealed type
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")

            classWriter.typeImport(serializerType)
            classWriter.typeAnnotation(
                "com.fasterxml.jackson.databind.annotation.JsonSerialize",
                "@JsonSerialize(using = ${serializerType.typeShortName()}::class)"
            )

            // the sub types
            typeMeta.sealedTypes.forEach { name, valueTypeName ->
                val subTypeName = DslTypeName(typeMeta.absoluteName + "_$name")

                classWriter.push(subTypeName)

                classWriter.typeAnnotation("javax.annotation.processing.Generated")

                classWriter.typeInterface(
                    "${typeMeta.name}()",
                    listOf(typeMeta.absoluteName)
                )

                classWriter.typeConstructorParameter(
                    listOf("val"),
                    "value",
                    valueTypeName
                )
            }
        }

        // the json serializer
        val kotlinSerializerWriter = KotlinClassWriter(serializerType, this.classWriterProducer)
        kotlinSerializerWriter.use { serializerWriter ->
            serializerWriter.typeAnnotation("javax.annotation.processing.Generated")

            serializerWriter.typeInterface(
                "JsonSerializer<${typeMeta.name}>()",
                listOf(
                    "com.fasterxml.jackson.databind.JsonSerializer"
                )
            )

            val serializerCode = mutableListOf<String>()
            val serializerTypeDependencies = mutableListOf(
                "com.fasterxml.jackson.core.JsonGenerator",
                "com.fasterxml.jackson.databind.SerializerProvider"
            )

            serializerCode.add("when (value) {")
            typeMeta.sealedTypes.forEach { name, valueTypeName ->
                val subTypeName = DslTypeName(typeMeta.absoluteName + "_$name")

                serializerTypeDependencies.add(subTypeName.absoluteName)

                val writeMethod = this.determineJsonWriteMethod(valueTypeName)

                serializerCode.add("    is ${subTypeName.typeShortName()} -> gen!!.$writeMethod(value.value)")
            }
            serializerCode.add("}")

            serializerWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "serialize",
                methodParameters = "value: ${typeMeta.name}?, gen: JsonGenerator?, serializers: SerializerProvider?",
                methodCode = serializerCode,
                methodTypeDependencies = serializerTypeDependencies
            )
        }

    }

    private fun generateInterfaceType(typeName : DslTypeName, typeMeta: DslInterfaceTypeMeta) {
        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)

        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
        }
    }

    /**
     * TODO this most likely does not cover everything
     */
    private fun determineJsonWriteMethod(typeName : DslTypeName) : String {
        return when {
            "Int" == typeName.typeShortName() -> "writeNumber"
            "String" == typeName.typeShortName() -> "writeString"
            else -> "writeObject"
        }
    }

}