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

package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
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
            is DslObjectTypeMeta -> this.generateObjectDsl(typeName, typeMeta, kotlinClassWriter)
            is DslContainerTypeMeta -> this.generateContainerDsl(typeName, typeMeta, kotlinClassWriter)
            is DslSealedTypeMeta -> this.generatedSealedDsl(typeName, typeMeta, kotlinClassWriter)
            is DslInterfaceTypeMeta -> this.generateInterfaceDsl(typeName, typeMeta, kotlinClassWriter)
            else -> throw IllegalStateException("Don't know what to do with " + typeMeta::javaClass)
        }
    }

    private fun generateInterfaceDsl(typeName : DslTypeName, typeMeta: DslInterfaceTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

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

            this.addGeneratorFunction(typeName, typeMeta, classWriter)
        }
    }

    private fun generatedSealedDsl(typeName : DslTypeName, typeMeta: DslSealedTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

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
                    methodParameters = listOf(
                        Pair("value", typeName.typeShortName())
                    ),
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

            this.addGeneratorFunction(typeName, typeMeta, classWriter)
        }
    }

    private fun generateContainerDsl(typeName : DslTypeName, typeMeta: DslContainerTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

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
                    methodParameters = listOf(
                        Pair("contained", typeMeta.containedType.typeShortName())
                    ),
                    methodCode = listOf(
                        "this.value = contained"
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

            this.addGeneratorFunction(typeName, typeMeta, classWriter)
        }
    }

    private fun generateObjectDsl(typeName : DslTypeName, typeMeta: DslObjectTypeMeta, kotlinClassWriter: KotlinClassWriter) {
        kotlinClassWriter.use { classWriter ->
            classWriter.typeDocumentation(typeMeta.description)

            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<${typeMeta.name}>",
                listOf(
                    "eu.rigeldev.kuberig.dsl.DslType",
                    typeMeta.absoluteName
                )
            )

            typeMeta.attributes.minus(attributeIgnores).forEach { rawAttributeName, attributeMeta ->

                val attributeName = classWriter.kotlinSafe(rawAttributeName)

                if (attributeMeta is DslListAttributeMeta) {
                    val listDslMeta = this.dslMeta.getListDslMeta(typeMeta, attributeMeta)

                    classWriter.typeAttribute(
                        listOf("private", "var"),
                        attributeName,
                        listDslMeta.declarationType(),
                        listDslMeta.declarationType().typeShortName() + "()",
                        attributeMeta.description
                    )

                    classWriter.typeMethod(
                        methodDocumentation = attributeMeta.description,
                        methodName = attributeName,
                        methodParameters = listOf(
                            Pair(attributeName, listDslMeta.declarationType().typeShortName())
                        ),
                        methodCode = listOf(
                            "this.$attributeName = $attributeName"
                        )
                    )

                    classWriter.typeMethod(
                        methodName = attributeMeta.getterMethodName(),
                        methodReturnType = listDslMeta.declarationType().typeShortName() + "?",
                        methodCode = listOf(
                            "return this.$attributeName"
                        )
                    )

                    if (listDslMeta.plural) {

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = listOf(
                                Pair("init","${listDslMeta.declarationType().typeShortName()}.() -> Unit")
                            ),
                            methodCode = listOf(
                                "this.$attributeName.init()"
                            )
                        )
                    }
                    else {
                        val listItemType = listDslMeta.dslItemType()

                        if (listDslMeta.complexItemType()) {
                            classWriter.typeMethod(
                                methodName = attributeName,
                                methodParameters = listOf(
                                    Pair("init", "${listItemType.typeShortName()}.() -> Unit")
                                ),
                                methodCode = listOf(
                                    "this.$attributeName.item(init)"
                                ),
                                methodTypeDependencies = listOf(
                                    listItemType.absoluteName
                                )
                            )
                        }
                        else {
                            classWriter.typeMethod(
                                methodDocumentation = attributeMeta.description,
                                methodName = attributeName,
                                methodParameters = listOf(
                                    Pair("value", listItemType.typeShortName())
                                ),
                                methodCode = listOf(
                                    "this.$attributeName.item(value)"
                                )
                            )
                        }

                    }

                }
                else if (attributeMeta is DslMapAttributeMeta) {
                    val mapDslMeta = this.dslMeta.getMapDslMeta(typeMeta, attributeMeta)

                    classWriter.typeAttribute(
                        listOf("private", "var"),
                        attributeName,
                        mapDslMeta.declarationType(),
                        mapDslMeta.declarationType().typeShortName() + "()",
                        attributeMeta.description
                    )

                    classWriter.typeMethod(
                        methodDocumentation = attributeMeta.description,
                        methodName = attributeName,
                        methodParameters = listOf(
                            Pair(attributeName, mapDslMeta.declarationType().typeShortName())
                        ),
                        methodCode = listOf(
                            "this.$attributeName = $attributeName"
                        )
                    )

                    classWriter.typeMethod(
                        methodName = attributeMeta.getterMethodName(),
                        methodReturnType = mapDslMeta.declarationType().typeShortName() + "?",
                        methodCode = listOf(
                            "return this.$attributeName"
                        )
                    )

                    if (mapDslMeta.plural) {
                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = listOf(
                                Pair("init","${mapDslMeta.declarationType().typeShortName()}.() -> Unit")
                            ),
                            methodCode = listOf(
                                "this.$attributeName.init()"
                            )
                        )
                    }
                    else {
                        val mapItemType = mapDslMeta.dslItemType()

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = listOf(
                                Pair("pair", "Pair<String, ${mapItemType.typeShortName()}>")
                            ),
                            methodCode = listOf(
                                "this.$attributeName.item(pair.first, pair.second)"
                            )
                        )

                        if (mapDslMeta.complexItemType()) {
                            classWriter.typeMethod(
                                methodName = attributeName,
                                methodParameters = listOf(
                                    Pair("key", "String"),
                                    Pair("init", "${mapItemType.typeShortName()}.() -> Unit")
                                ),
                                methodCode = listOf(
                                    "this.$attributeName.item(key, init)"
                                ),
                                methodTypeDependencies = listOf(
                                    mapItemType.absoluteName
                                )
                            )
                        } else {
                            classWriter.typeMethod(
                                methodDocumentation = attributeMeta.description,
                                methodName = attributeName,
                                methodParameters = listOf(
                                    Pair("key", "String"),
                                    Pair("value", mapItemType.typeShortName())
                                ),
                                methodCode = listOf(
                                    "this.$attributeName.item(key, value)"
                                )
                            )
                        }
                    }
                }
                else if (attributeMeta is DslObjectAttributeMeta) {

                    if (attributeMeta.absoluteType.requiresImport() && attributeMeta.absoluteType.isNotPlatformType()) {

                        val declarationType = DslTypeName(attributeMeta.absoluteType.absoluteName + "Dsl")

                        classWriter.typeAttribute(
                            listOf("private", "var"),
                            attributeName,
                            declarationType,
                            "null",
                            nullable = true
                        )

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = listOf(
                                Pair(attributeName, declarationType.typeShortName())
                            ),
                            methodCode = listOf(
                                "this.$attributeName = $attributeName"
                            )
                        )

                        classWriter.typeMethod(
                            methodName = attributeMeta.getterMethodName(),
                            methodReturnType = declarationType.typeShortName() + "?",
                            methodCode = listOf(
                                "return this.$attributeName"
                            )
                        )

                        val attributeTypeMeta = this.dslMeta.typeMeta[attributeMeta.absoluteType.absoluteName]

                        if (attributeTypeMeta is DslSealedTypeMeta) {
                            attributeTypeMeta.sealedTypes.values.forEach { typeName ->
                                classWriter.typeMethod(
                                    methodDocumentation = attributeMeta.description,
                                    methodName = attributeName,
                                    methodParameters = listOf(
                                        Pair(attributeName, typeName.typeShortName())
                                    ),
                                    methodCode = listOf(
                                        "this.$attributeName = ${attributeMeta.attributeDeclarationType()}Dsl()",
                                        "this.$attributeName!!.value($attributeName)"
                                    )
                                )
                            }
                        }
                        else {

                            classWriter.typeMethod(
                                methodDocumentation = attributeMeta.description,
                                methodName = attributeName,
                                methodParameters = listOf(
                                    Pair("init", "${attributeMeta.attributeDeclarationType()}Dsl.() -> Unit")
                                ),
                                methodCode = listOf(
                                    "val attr = ${attributeMeta.attributeDeclarationType()}Dsl()",
                                    "attr.init()",
                                    "this.$attributeName = attr"
                                )
                            )
                        }

                    } else {

                        classWriter.typeAttribute(
                            listOf("private", "var"),
                            attributeName,
                            attributeMeta.absoluteType,
                            "null",
                            attributeMeta.description,
                            true
                        )

                        classWriter.typeMethod(
                            methodDocumentation = attributeMeta.description,
                            methodName = attributeName,
                            methodParameters = listOf(
                                Pair(attributeName, attributeMeta.attributeDeclarationType())
                            ),
                            methodCode = listOf(
                                "this.$attributeName = $attributeName"
                            )
                        )

                        classWriter.typeMethod(
                            methodName = attributeMeta.getterMethodName(),
                            methodReturnType = attributeMeta.absoluteType.typeShortName() + "?",
                            methodCode = listOf(
                                "return this.$attributeName"
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
                val (rawAttributeName, attributeMeta) = constructorIt.next()

                val attributeName = classWriter.kotlinSafe(rawAttributeName)
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

            this.addGeneratorFunction(typeName, typeMeta, classWriter)
        }
    }

    private fun addGeneratorFunction(dslTypeName: DslTypeName, typeMeta: DslTypeMeta, classWriter: KotlinClassWriter) {
        val declarationType = DslTypeName(typeMeta.absoluteName)

        classWriter.fileMethod(
            methodName = declarationType.methodName(),
            methodParameters = listOf(
                Pair("init", "${dslTypeName.typeShortName()}.() -> Unit")
            ),
            methodReturnType = dslTypeName.typeShortName(),
            methodCode = listOf(
                "val gen = ${dslTypeName.typeShortName()}()",
                "gen.init()",
                "return gen"
            )
        )
    }

}