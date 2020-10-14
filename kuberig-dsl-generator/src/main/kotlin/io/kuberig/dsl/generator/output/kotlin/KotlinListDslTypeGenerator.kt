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

import io.kuberig.dsl.generator.meta.DslTypeName
import io.kuberig.dsl.generator.meta.collections.DslListDslMeta

class KotlinListDslTypeGenerator(private val classWriterProducer : KotlinClassWriterProducer,
                                 private val factoryMethodsGenerator: KotlinFactoryMethodsGenerator) {

    fun generateListDslType(listDslMeta: DslListDslMeta) {
        KotlinClassWriter(listDslMeta.declarationType(), this.classWriterProducer).use { classWriter ->
            val resultListItemType = listDslMeta.meta.itemType.typeShortName()

            classWriter.typeAnnotation("io.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<List<$resultListItemType>>",
                listOf("io.kuberig.dsl.DslType", listDslMeta.meta.itemType.absoluteName)
            )

            val listItemType = listDslMeta.dslItemType().typeShortName()

            classWriter.listTypeAttribute(
                listOf("private", "val"),
                "list",
                DslTypeName("MutableList"),
                DslTypeName(listDslMeta.dslItemType().absoluteName),
                "mutableListOf()"
            )

            val addMethodName = classWriter.kotlinSafe(listDslMeta.addMethodName())

            classWriter.typeMethod(
                methodName = addMethodName,
                methodParameters = listOf(
                    Pair(addMethodName, listItemType)
                ),
                methodCode = listOf(
                    "this.list.add($addMethodName)"
                )
            )

            if (listDslMeta.complexItemType()) {
                classWriter.typeMethod(
                    methodName = addMethodName,
                    methodParameters = listOf(
                        Pair("init", "$listItemType.() -> Unit")
                    ),
                    methodCode = listOf(
                        "val item = $listItemType()",
                        "item.init()",
                        "this.list.add(item)"
                    )
                )

                classWriter.typeMethod(
                    modifiers = listOf("override"),
                    methodName = "toValue",
                    methodReturnType = "List<$resultListItemType>",
                    methodCode = listOf(
                        "return Collections.unmodifiableList(this.list.stream()",
                        "    .map($listItemType::toValue)",
                        "    .collect(Collectors.toList()))"
                    ),
                    methodTypeDependencies = listOf(
                        "java.util.stream.Collectors",
                        "java.util.Collections"
                    )
                )
            }
            else {
                classWriter.typeMethod(
                    modifiers = listOf("override"),
                    methodName = "toValue",
                    methodReturnType = "List<$resultListItemType>",
                    methodCode = listOf(
                        "return Collections.unmodifiableList(this.list)"
                    ),
                    methodTypeDependencies = listOf(
                        "java.util.Collections"
                    )
                )
            }

            factoryMethodsGenerator.addFactoryMethod(
                listDslMeta.declarationType().methodName(),
                listDslMeta.declarationType()
            )
        }
    }
}