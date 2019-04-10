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

package eu.rigeldev.kuberig.dsl.generator.input.swagger

import eu.rigeldev.kuberig.dsl.generator.input.DslMetaProducer
import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslPlatformSpecifics
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslContainerTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslInterfaceTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslObjectTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslSealedTypeMeta
import io.swagger.models.ModelImpl
import io.swagger.models.RefModel
import io.swagger.models.Swagger
import io.swagger.models.properties.*
import io.swagger.parser.SwaggerParser
import java.io.File

class SwaggerDslMetaProducer(private val swaggerFile: File) : DslMetaProducer {

    private lateinit var spec: Swagger
    private lateinit var dslMeta : DslMeta

    var showIgnoredRefModels : Boolean = false

    override fun provide(): DslMeta {
        this.spec = SwaggerParser().read(swaggerFile.absolutePath)

        if (spec.info.title.toLowerCase().contains("openshift")) {
            this.dslMeta = DslMeta(
                DslPlatformSpecifics(
                    listOf("io.k8s", "com.github")
                )
            )
        } else {
            this.dslMeta = DslMeta(
                DslPlatformSpecifics(
                    listOf("io.k8s")
                )
            )
        }

        this.processDefinitions()

        return dslMeta
    }

    private fun processDefinitions() {

        spec.definitions.forEach { name, definition ->
            if (definition is ModelImpl) {
                generateDslClass(name, definition)
            } else if (definition is RefModel) {
                // the description from the swagger file is not available
                // because the swagger parser does not allow a description on a RefModel

                // nothing to do - definition is deprecated, show message if toggled.
                if (showIgnoredRefModels) {
                    println("Ignoring definition $name use ${definition.simpleRef} instead.")
                }

            } else {
                println("do not yet know how to handle a " + definition.javaClass)
            }

        }

    }

    private fun generateDslClass(absoluteName: String, model: ModelImpl) {
        if (model.type == "object" || (model.properties != null && model.properties.isNotEmpty())) {
            generateDslClassInternal(absoluteName, model)
        } else if (model.type == "string") {
            generateDslClassInternal(absoluteName, model)
        } else {
            if (model.description != null && !model.description.startsWith("Deprecated")) {
                generateDslClassInternal(absoluteName, model)
            } else {
                println("[SKIPPED] $absoluteName is not an object")
            }
        }
    }

    private fun generateDslClassInternal(rawName: String, model: ModelImpl) {
        val absoluteName = rawName.replace('-', '.')

        val packageName = this.packageName(absoluteName)
        val name = this.className(absoluteName)
        val documentation = model.description ?: ""


        if (model.type == "object" || (model.properties != null && model.properties.isNotEmpty())) {
            val typeDependencies = determineModelTypeDependencies(model)
            val attributes = determineModelAttributes(model)

            this.dslMeta.registerType(
                DslObjectTypeMeta(
                    absoluteName,
                    packageName,
                    name,
                    documentation,
                    typeDependencies,
                    attributes
                )
            )
        }
        else if (model.type == "string") {

            when {
                model.format == null -> this.dslMeta.registerType(
                    DslContainerTypeMeta(
                        absoluteName,
                        packageName,
                        name,
                        documentation,
                        emptySet(),
                        DslTypeName("String")
                    )
                )
                model.format == "date-time" -> {
                    val containedType = DslTypeName("java.time.ZonedDateTime")
                    this.dslMeta.registerType(
                        DslContainerTypeMeta(
                            absoluteName,
                            packageName,
                            name,
                            documentation,
                            setOf(containedType),
                            containedType
                        )
                    )
                }
                model.format.contains("-or-") -> {
                    val sealedTypes = mutableMapOf<String, DslTypeName>()

                    val splits = model.format.split("-or-")

                    for (split in splits) {
                        var valueType = ""
                        if (split == "int") {
                            valueType = "Int"
                        } else if (split == "string") {
                            valueType = "String"
                        }

                        if (valueType == "") {
                            throw IllegalStateException("Don't know how to handle type split [$split] for $absoluteName")
                        } else {
                            sealedTypes[split.toLowerCase()] =
                                DslTypeName(valueType)
                        }
                    }

                    this.dslMeta.registerType(
                        DslSealedTypeMeta(
                            absoluteName,
                            packageName,
                            name,
                            documentation,
                            emptySet(),
                            sealedTypes
                        )
                    )
                }
                else -> println("[SKIPPED] $absoluteName don't know how to handle format: ${model.format}")
            }

        }
        else {
            this.dslMeta.registerType(
                DslInterfaceTypeMeta(
                    absoluteName,
                    packageName,
                    name,
                    documentation,
                    emptySet()
                )
            )
        }

        this.registerModelKinds(model, absoluteName)
    }

    private fun determineModelTypeDependencies(model: ModelImpl): Set<DslTypeName> {
        val typeDependencies = mutableSetOf<DslTypeName>()

        if (model.properties != null) {
            model.properties.forEach { _, property ->

                var typeDependency: DslTypeName? = null
                when {
                    isObjectProperty(property) -> typeDependency = this.kotlinTypeAbsolute(property)
                    isListProperty(property) -> typeDependency = this.listPropertyItemType(property)
                    isMapProperty(property) -> typeDependency = this.mapPropertyValueType(property)
                }

                if (typeDependency != null && typeDependency.requiresImport()) {
                    typeDependencies.add(typeDependency)
                }
            }
        }

        return typeDependencies
    }

    private fun determineModelAttributes(model: ModelImpl): Map<String, DslAttributeMeta> {
        val attributes = mutableMapOf<String, DslAttributeMeta>()

        if (model.properties != null) {
            model.properties.forEach { name, property ->

                val documentation = property.description ?: ""
                val required = this.isPropertyRequired(model, property)

                if (!documentation.toLowerCase().contains("read-only")) {
                    if (this.isListProperty(property)) {
                        val itemType = this.listPropertyItemType(property)

                        if (itemType != null) {
                            attributes[name] = DslListAttributeMeta(
                                name,
                                documentation,
                                required,
                                itemType
                            )
                        }
                    } else if (this.isMapProperty(property)) {
                        val itemType = this.mapPropertyValueType(property)

                        if (itemType != null) {
                            attributes[name] = DslMapAttributeMeta(
                                name,
                                documentation,
                                required,
                                // Swagger file does not provide key type information
                                // https://swagger.io/docs/specification/data-models/dictionaries/
                                // fragment: "...OpenAPI lets you define dictionaries where the keys are strings..."
                                DslTypeName("String"),
                                itemType
                            )
                        }
                    } else {
                        val type = this.kotlinTypeAbsolute(property)

                        if (type != null) {
                            attributes[name] = DslObjectAttributeMeta(
                                name,
                                documentation,
                                required,
                                type
                            )
                        }
                    }
                }


            }
        }


        return attributes
    }

    private fun isPropertyRequired(model: ModelImpl, property: Property): Boolean {
        return if (model.required == null) {
            false
        } else {
            model.required.contains(property.name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerModelKinds(model: ModelImpl, absoluteName: String) {
        if (model.vendorExtensions.containsKey("x-kubernetes-group-version-kind")) {
            val kinds = model.vendorExtensions["x-kubernetes-group-version-kind"]!! as List<LinkedHashMap<*, *>>

            kinds.forEach { groupVersionKind ->
                val normalizedGroupVersionKind = groupVersionKind.mapKeys { (it.key as String).toLowerCase() }

                dslMeta.registerKind(
                    DslKindMeta(
                        DslTypeName(absoluteName),
                        normalizedGroupVersionKind["group"] as String,
                        normalizedGroupVersionKind["kind"] as String,
                        normalizedGroupVersionKind["version"] as String
                    )
                )
            }
        }
    }

    private fun packageName(absoluteName: String): String {
        val allSplits = absoluteName.split(".", "-")
        return allSplits.subList(0, allSplits.size - 1).joinToString(separator = ".")
    }

    private fun className(absoluteName: String): String {
        return absoluteName.split(".", "-").last()
    }

    private fun isObjectProperty(property: Property): Boolean {
        return property is RefProperty
    }

    private fun isListProperty(property: Property): Boolean {
        return property is ArrayProperty
    }

    private fun listPropertyItemType(property: Property): DslTypeName? {
        if (isListProperty(property)) {
            val listProperty = property as ArrayProperty

            return this.kotlinTypeAbsolute(listProperty.items)
        } else {
            throw IllegalArgumentException("property is not of correct type!" + property::javaClass)
        }
    }

    private fun isMapProperty(property: Property): Boolean {
        return property is MapProperty
    }

    private fun mapPropertyValueType(property: Property): DslTypeName? {
        if (isMapProperty(property)) {
            val mapProperty = property as MapProperty

            return if (this.isListProperty(mapProperty.additionalProperties)) {
                this.listPropertyItemType(mapProperty.additionalProperties)
            } else {
                this.kotlinTypeAbsolute(mapProperty.additionalProperties)
            }

        } else {
            throw IllegalArgumentException("property is not of correct type!" + property::javaClass)
        }
    }

    private fun kotlinTypeAbsolute(property: Property): DslTypeName? {
        var propertyType = ""

        when (property) {
            is RefProperty ->
                // compensate for apiextensions-apiserver in package name
                propertyType = property.simpleRef.replace('-', '.')
            is StringProperty ->
                propertyType = if (property.format == "byte") {
                    // TODO not sure this is the best type - but will do for now (shows we can detect the difference).
                    "ByteArray"
                } else {
                    "String"
                }
            is LongProperty -> propertyType = "Long"
            is IntegerProperty -> propertyType = "Int"
            is BooleanProperty -> propertyType = "Boolean"
            is DoubleProperty -> propertyType = "Double"
            is ArrayProperty -> throw IllegalStateException("should not be called for ArrayProperty")
            is MapProperty -> throw IllegalStateException("should not be called for MapProperty")
            else -> println("[WARN] unhandled property ${property.name} of type ${property.type}")
        }

        return if (propertyType == "") {
            null
        } else {
            DslTypeName(propertyType)
        }
    }
}