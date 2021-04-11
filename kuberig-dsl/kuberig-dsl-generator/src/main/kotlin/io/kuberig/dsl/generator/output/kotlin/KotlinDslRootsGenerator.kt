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
import io.kuberig.dsl.generator.meta.kinds.DslKindContainer

class KotlinDslRootsGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    private lateinit var dslMeta: DslMeta

    fun generateDslRoots(dslMeta : DslMeta) {
        this.dslMeta = dslMeta
        val dslKindContainer = buildContainer()

        this.generateDslRootType(
            this.typeName(dslKindContainer),
            dslKindContainer
        )
    }

    private fun generateDslRootType(typeName : DslTypeName, container : DslKindContainer) {
        KotlinClassWriter(typeName, this.classWriterProducer).use { classWriter ->

            classWriter.typeAnnotation("io.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeConstructorParameter(
                listOf("private", "val"),
                "sink",
                DslTypeName("io.kuberig.dsl.DslResourceSink")
            )

            // dsl attributes and methods for sub containers
            container.subContainers.forEach { (subName, subContainer) ->
                val subContainerType = this.typeName(subContainer, typeName.packageName())

                classWriter.typeAttribute(
                    listOf("val"),
                    subName,
                    subContainerType,
                    "${subContainerType.typeShortName()}(this.sink)"
                )

                // dsl methods for sub containers
                classWriter.typeMethod(
                    methodName = subName,
                    methodParameters = listOf(
                        Pair("init","${classWriter.kotlinSafe(subContainerType.typeShortName())}.() -> Unit")
                    ),
                    methodCode = listOf(
                        "this.${classWriter.kotlinSafe(subName)}.init()"
                    ),
                    methodTypeDependencies = listOf(
                        subContainerType.absoluteName
                    )
                )
            }

            // dsl methods for kinds
            container.kinds.forEach { (_, kindMeta) ->
                val kindType = kindMeta.kindType().typeShortName()
                val kindMethodName = kindMeta.methodName()

                classWriter.typeMethod(
                    methodName = kindMethodName,
                    methodParameters = listOf(
                        Pair("alias", "String"),
                        Pair("init", "$kindType.() -> Unit")
                    ),
                    methodCode = listOf(
                        "val dsl = ${kindType}()",
                        "dsl.init()",
                        "this.sink.add(DslResource(alias, dsl))"
                    ),
                    methodTypeDependencies = listOf(
                        "io.kuberig.dsl.DslResource",
                        kindMeta.kindType().absoluteName
                    )
                )

                classWriter.typeMethod(
                    methodName = kindMethodName,
                    methodParameters = listOf(
                        Pair("alias", "String"),
                        Pair(kindMethodName, kindType)
                    ),
                    methodCode = listOf(
                        "this.sink.add(DslResource(alias, $kindMethodName))"
                    ),
                    methodTypeDependencies = listOf(
                        kindMeta.kindType().absoluteName
                    )
                )
            }

        }

        // generate sub containers
        container.subContainers.forEach { (_, subContainer) ->
            this.generateDslRootType(
                this.typeName(subContainer, typeName.packageName()),
                subContainer
            )
        }
    }

    private fun buildContainer() : DslKindContainer {
        val dslKindContainer = DslKindContainer("kinds")

        dslMeta.kindMeta.forEach(dslKindContainer::add)

        return dslKindContainer
    }

    private fun typeName(container : DslKindContainer, parentPackage : String = "") : DslTypeName {
        val absolutePackage = if (parentPackage == ""){
            container.name
        } else {
            "$parentPackage.${container.name}"
        }

        return DslTypeName("$absolutePackage.${container.typeName()}")
    }
}