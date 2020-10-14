/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kuberig.dsl.generator.output.kotlin

import io.kuberig.dsl.generator.meta.DslMeta
import io.kuberig.dsl.generator.meta.DslTypeName
import io.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import io.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import io.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
import io.kuberig.dsl.generator.meta.types.*

class KotlinApiTypeGenerator(
        private val classWriterProducer : KotlinClassWriterProducer,
        private val dslMeta: DslMeta) {

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

        val fullMetaDataTypeName = this.dslMeta.resourceMetadataType
        if (fullMetaDataTypeName.absoluteName == typeName.absoluteName) {
            kotlinClassWriter.typeParent(
                DslTypeName("io.kuberig.dsl.model.FullMetadata"),
                listOf("namespace", "name", "annotations", "labels")
            )
        }

        if (typeMeta.kindType) {
            val metadataAttribute = typeMeta.attributes["metadata"]
            val fullResource = if(metadataAttribute != null) {
                metadataAttribute.absoluteAttributeDeclarationType().absoluteName == fullMetaDataTypeName.absoluteName
            } else {
                false
            }

            if (fullResource) {
                kotlinClassWriter.typeParent(
                        DslTypeName("io.kuberig.dsl.model.FullResource"),
                        listOf("kind", "apiVersion", "metadata")
                )
            } else {
                kotlinClassWriter.typeParent(
                        DslTypeName("io.kuberig.dsl.model.BasicResource"),
                        listOf("kind", "apiVersion")
                )
            }
        }

        kotlinClassWriter.use {classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            typeMeta.attributes.minus("status").forEach { (attributeName, attributeMeta) ->

                val nullable = attributeMeta.isOptional()

                when (attributeMeta) {
                    is DslObjectAttributeMeta -> {
                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = nullable
                        )
                    }
                    is DslListAttributeMeta -> {
                        classWriter.typeImport(attributeMeta.itemType)

                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = nullable,
                            declarationTypeOverride = attributeMeta.attributeDeclarationType()
                        )
                    }
                    is DslMapAttributeMeta -> {
                        classWriter.typeImport(attributeMeta.itemType)

                        classWriter.typeConstructorParameter(
                            listOf("val"),
                            attributeName,
                            attributeMeta.absoluteAttributeDeclarationType(),
                            nullable = nullable,
                            declarationTypeOverride = attributeMeta.attributeDeclarationType()
                        )
                    }
                    else -> throw IllegalStateException("Don't know what to do with " + attributeMeta::javaClass)
                }
            }
        }

    }

    private fun generateContainerType(typeName : DslTypeName, typeMeta: DslContainerTypeMeta) {
        val serializerType = DslTypeName(
            typeName.packageName() + "." + typeMeta.typeName.typeShortName() + "_Serializer"
        )

        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)
        // the container type
        kotlinClassWriter.use {classWriter ->

            classWriter.typeDocumentation(typeMeta.description)

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

            serializerWriter.typeInterface(
                "JsonSerializer<${typeMeta.typeName.typeShortName()}>()",
                listOf(
                    "com.fasterxml.jackson.databind.JsonSerializer"
                )
            )

            val writeMethod = this.determineJsonWriteMethod(typeMeta.containedType)

            serializerWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "serialize",
                methodParameters = listOf(
                    Pair("value", "${typeMeta.typeName.typeShortName()}?"),
                    Pair("gen", "JsonGenerator?"),
                    Pair("serializers", "SerializerProvider?")
                ),
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
            typeName.packageName() + "." + typeMeta.typeName.typeShortName() + "_Serializer"
        )

        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer, "sealed class")
        // the sealed type
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeImport(serializerType)
            classWriter.typeAnnotation(
                "com.fasterxml.jackson.databind.annotation.JsonSerialize",
                "@JsonSerialize(using = ${serializerType.typeShortName()}::class)"
            )

            // the sub types
            typeMeta.sealedTypes.forEach { (name, valueTypeName) ->
                val subTypeName = DslTypeName(typeMeta.typeName.absoluteName + "_$name")

                classWriter.push(subTypeName)

                classWriter.typeInterface(
                    "${typeMeta.typeName.typeShortName()}()",
                    listOf(typeMeta.typeName.absoluteName)
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

            serializerWriter.typeInterface(
                "JsonSerializer<${typeMeta.typeName.typeShortName()}>()",
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
            typeMeta.sealedTypes.forEach { (name, valueTypeName) ->
                val subTypeName = DslTypeName(typeMeta.typeName.absoluteName + "_$name")

                serializerTypeDependencies.add(subTypeName.absoluteName)

                val writeMethod = this.determineJsonWriteMethod(valueTypeName)

                serializerCode.add("    is ${subTypeName.typeShortName()} -> gen!!.$writeMethod(value.value)")
            }
            serializerCode.add("}")

            serializerWriter.typeMethod(
                modifiers = listOf("override"),
                methodName = "serialize",
                methodParameters = listOf(
                    Pair("value", "${typeMeta.typeName.typeShortName()}?"),
                    Pair("gen", "JsonGenerator?"),
                    Pair("serializers", "SerializerProvider?")
                ),
                methodCode = serializerCode,
                methodTypeDependencies = serializerTypeDependencies
            )
        }

    }

    private fun generateInterfaceType(typeName : DslTypeName, typeMeta: DslInterfaceTypeMeta) {
        val kotlinClassWriter = KotlinClassWriter(typeName, this.classWriterProducer)

        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

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