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
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.Kind
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.KindTypes
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.KindUrl
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslContainerTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslInterfaceTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslObjectTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslSealedTypeMeta
import io.swagger.models.Model
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

    private val additionalPropertyDefinitions = mutableMapOf<String, DslClassInfo>()

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

        this.findWritableKindUrls()
        this.findWritableKindTypes()

        this.processKindTypes();
        this.processAdditionalDefinitions()

        return dslMeta
    }

    private fun findWritableKindUrls() {
        this.spec.paths.forEach { (url, path) ->

            if (path.post != null) {
                if (path.post.vendorExtensions.containsKey("x-kubernetes-group-version-kind")) {
                    val rawMap = path.post.vendorExtensions["x-kubernetes-group-version-kind"] as Map<*, *>

                    val kind = kubernetesGroupVersionKindToKind(rawMap)

                    val kindUrl = KindUrl(url, kind)

                    this.dslMeta.writeableKindUrls[kind] = kindUrl
                }
            }
        }

        println("Found #${this.dslMeta.writeableKindUrls.size} writeable kinds...")
    }

    private fun findWritableKindTypes() {
        this.dslMeta.writeableKindUrls.keys.forEach { kind ->
            val types = mutableListOf<String>()

            this.spec.definitions.forEach { (absoluteTypeName, definition) ->

                if (definition.vendorExtensions.containsKey("x-kubernetes-group-version-kind")) {
                    val groupVersionKindList = definition.vendorExtensions["x-kubernetes-group-version-kind"]!! as List<Map<*, *>>

                    groupVersionKindList.forEach { groupVersionKind ->
                        val definitionKind = this.kubernetesGroupVersionKindToKind(groupVersionKind)

                        if (definitionKind == kind) {
                            types.add(absoluteTypeName)
                        }
                    }
                }
            }

            if (types.isNotEmpty()) {
                this.dslMeta.writeableKindTypes[kind] = KindTypes(kind, types)
            }
        }
    }

    private fun kubernetesGroupVersionKindToKind(rawMap: Map<*, *>): Kind {

        val normalizedGroupVersionKind = rawMap.mapKeys { (it.key as String).toLowerCase() }

        val kind = Kind(
                (normalizedGroupVersionKind["group"] ?: error("No group found group-version-kind")) as String,
                (normalizedGroupVersionKind["kind"] ?: error("No kind found group-version-kind")) as String,
                (normalizedGroupVersionKind["version"] ?: error("No version found group-version-kind")) as String
        )

        return kind
    }

    private fun processKindTypes() {

        check(this.dslMeta.writeableKindTypes.isNotEmpty()) { "No writeable kinds found, no DSL code to generate." }

        // determine the actual type of the metadata attribute of a Kind
        val firstKindTypes = this.dslMeta.writeableKindTypes.values.toList()[0]
        val firstTypeName = firstKindTypes.types[0]
        val firstDefinition = spec.definitions[firstTypeName]!!
        val metadataAttribute = firstDefinition.properties["metadata"]!! as RefProperty
        this.dslMeta.resourceMetadataType = DslTypeName(metadataAttribute.simpleRef)

        // create type-meta for kind classes
        this.dslMeta.writeableKindTypes.values.forEach { kindTypes ->

            kindTypes.types.forEach { typeName ->

                val definition = spec.definitions[typeName]!!

                processDefinition(typeName, definition, true)

                dslMeta.registerKind(
                        DslKindMeta(
                                DslTypeName(typeName),
                                kindTypes.kind.group,
                                kindTypes.kind.kind,
                                kindTypes.kind.version
                        )
                )
            }
        }
    }

    private fun processDefinition(typeName: String, definition: Model, kindType: Boolean) {
        println("[PROCESS] $typeName")

        if (definition is ModelImpl) {
            generateDslClass(typeName, DslClassInfoModelImplAdapter.toDslClassInfo(definition), kindType)
        } else if (definition is RefModel) {
            // the description from the swagger file is not available
            // because the swagger parser does not allow a description on a RefModel

            // nothing to do - definition is deprecated, show message if toggled.
            if (showIgnoredRefModels) {
                println("Ignoring definition ${typeName} use ${definition.simpleRef} instead.")
            }

        } else {
            println("do not yet know how to handle a " + definition::class.java)
        }
    }

    /**
     * CRD definitions do not always provide dedicated definitions for objects.
     *
     * During generation we detect these 'missing' models and generate them after the main definitions are processed.
     *
     * As this can be a recursive problem, generating the 'missing' models is tried until no new 'missing' definitions
     * are added during a generate DSL class iteration.
     */
    private fun processAdditionalDefinitions() {
        var definitionsCompensatedCount = 0

        while (this.additionalPropertyDefinitions.isNotEmpty()) {

            val toProcess = mutableMapOf<String, DslClassInfo>()
            toProcess.putAll(this.additionalPropertyDefinitions)
            this.additionalPropertyDefinitions.clear()

            toProcess.forEach { (name, dslClassInfo) ->
                val kind = this.dslMeta.kindType(name)

                if (kind == null) {
                    generateDslClass(name, dslClassInfo, false)
                } else {
                    generateDslClass(name, dslClassInfo, true)
                }
            }

            definitionsCompensatedCount += toProcess.size

        }
    }

    private fun generateDslClass(absoluteName: String, dslClassInfo: DslClassInfo, kindType: Boolean) {
        if (dslClassInfo.type == "object" || dslClassInfo.properties.isNotEmpty()) {
            generateDslClassInternal(absoluteName, dslClassInfo, kindType)
        } else if (dslClassInfo.type == "string") {
            generateDslClassInternal(absoluteName, dslClassInfo, kindType)
        } else {
            if (dslClassInfo.description != null && !dslClassInfo.description.startsWith("Deprecated")) {
                generateDslClassInternal(absoluteName, dslClassInfo, kindType)
            } else {
                println("[SKIPPED] $absoluteName is not an object")
            }
        }
    }

    private fun generateDslClassInternal(rawName: String, dslClassInfo: DslClassInfo, kindType: Boolean) {
        val typeName = DslTypeName(rawName)
        val absoluteName = typeName.absoluteName

        val packageName = typeName.packageName()
        val name = typeName.typeShortName()
        val documentation = dslClassInfo.description ?: ""

        if (dslClassInfo.type == "object" || dslClassInfo.properties.isNotEmpty()) {
            val typeDependencies = determineModelTypeDependencies(dslClassInfo, absoluteName)
            val attributes = determineModelAttributes(dslClassInfo, absoluteName)

            typeDependencies.forEach { dependentRawName ->
                if (!this.dslMeta.typeMeta.containsKey(DslTypeName(dependentRawName).absoluteName)) {
                    if (!this.additionalPropertyDefinitions.containsKey(dependentRawName)) {
                        val definition = this.spec.definitions[dependentRawName]
                        if (definition != null && definition is ModelImpl) {
                            this.additionalPropertyDefinitions[dependentRawName] = DslClassInfoModelImplAdapter.toDslClassInfo(definition)
                        }
                    }
                }
            }

            this.dslMeta.registerType(
                DslObjectTypeMeta(
                    absoluteName,
                    packageName,
                    name,
                    documentation,
                    typeDependencies.map(::DslTypeName).toSet(),
                    attributes,
                    kindType
                )
            )
        }
        else if (dslClassInfo.type == "string") {

            when {
                dslClassInfo.format == null -> this.dslMeta.registerType(
                    DslContainerTypeMeta(
                        absoluteName,
                        packageName,
                        name,
                        documentation,
                        emptySet(),
                        DslTypeName("String")
                    )
                )
                dslClassInfo.format == "date-time" -> {
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
                dslClassInfo.format.contains("-or-") -> {
                    val sealedTypes = mutableMapOf<String, DslTypeName>()

                    val splits = dslClassInfo.format.split("-or-")

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
                else -> println("[SKIPPED] $absoluteName don't know how to handle format: ${dslClassInfo.format}")
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
    }

    private fun determineModelTypeDependencies(dslClassInfo: DslClassInfo, absoluteName: String): Set<String> {
        val typeDependencies = mutableSetOf<String>()

        dslClassInfo.properties.forEach { name, property ->

            var typeDependency: String? = null
            when {
                isObjectProperty(property) -> typeDependency = this.kotlinTypeAbsolute(property, absoluteName, name)
                isListProperty(property) -> typeDependency = this.listPropertyItemType(property, absoluteName, name)
                isMapProperty(property) -> typeDependency = this.mapPropertyValueType(property, absoluteName, name)
            }

            if (typeDependency != null && DslTypeName(typeDependency).requiresImport()) {
                typeDependencies.add(typeDependency)
            }
        }

        return typeDependencies
    }

    private fun determineModelAttributes(dslClassInfo: DslClassInfo, absoluteName: String): Map<String, DslAttributeMeta> {
        val attributes = mutableMapOf<String, DslAttributeMeta>()

        var propertyPosition = 0
        dslClassInfo.properties.forEach { (name, property) ->

            val documentation = property.description ?: ""
            val required = this.isPropertyRequired(dslClassInfo, property)

            if (!documentation.toLowerCase().contains("read-only")) {
                propertyPosition++

                if (this.isListProperty(property)) {
                    val itemType = this.listPropertyItemType(property, absoluteName, name)

                    if (itemType != null) {
                        attributes[name] = DslListAttributeMeta(
                            name,
                            documentation,
                            required,
                            DslTypeName(itemType)
                        )
                    }
                } else if (this.isMapProperty(property)) {
                    val itemType = this.mapPropertyValueType(property, absoluteName, name)

                    if (itemType != null) {
                        attributes[name] = DslMapAttributeMeta(
                            name,
                            documentation,
                            required,
                            // Swagger file does not provide key type information
                            // https://swagger.io/docs/specification/data-models/dictionaries/
                            // fragment: "...OpenAPI lets you define dictionaries where the keys are strings..."
                            DslTypeName("String"),
                            DslTypeName(itemType)
                        )
                    }
                } else {
                    val type = this.kotlinTypeAbsolute(property, absoluteName, name)

                    if (type != null) {
                        attributes[name] = DslObjectAttributeMeta(
                                name,
                                documentation,
                                required,
                                DslTypeName(type)
                        )
                    }
                }
            }

        }


        return attributes
    }

    private fun isPropertyRequired(dslClassInfo: DslClassInfo, property: Property): Boolean {
        return dslClassInfo.required.contains(property.name)
    }

    private fun isObjectProperty(property: Property): Boolean {
        return property is RefProperty || property is ObjectProperty
    }

    private fun isListProperty(property: Property): Boolean {
        return property is ArrayProperty
    }

    private fun listPropertyItemType(property: Property, owningTypeName: String, listPropertyName: String): String? {
        if (isListProperty(property)) {
            val listProperty = property as ArrayProperty

            return this.kotlinTypeAbsolute(listProperty.items, owningTypeName, listPropertyName + "Item")
        } else {
            throw IllegalArgumentException("property is not of correct type!" + property::javaClass)
        }
    }

    private fun isMapProperty(property: Property): Boolean {
        return property is MapProperty
    }

    private fun mapPropertyValueType(property: Property, owningTypeName: String, mapPropertyName: String): String? {
        if (isMapProperty(property)) {
            val mapProperty = property as MapProperty

            return if (this.isListProperty(mapProperty.additionalProperties)) {
                this.listPropertyItemType(mapProperty.additionalProperties, owningTypeName, mapPropertyName + "Value")
            } else {
                this.kotlinTypeAbsolute(mapProperty.additionalProperties, owningTypeName, mapPropertyName + "Value")
            }

        } else {
            throw IllegalArgumentException("property is not of correct type!" + property::javaClass)
        }
    }

    private fun kotlinTypeAbsolute(property: Property, owningTypeName: String, owningAttributeName: String): String? {
        var propertyType : String? = null

        when (property) {
            is RefProperty ->
                propertyType = property.simpleRef
            is StringProperty ->
                propertyType = if (property.format == "byte") {
                    // TODO not sure this is the best type - but will do for now (shows we can detect the difference).
                    "ByteArray"
                } else {
                    "String"
                }
            is LongProperty -> propertyType = "Long"
            is IntegerProperty -> propertyType = "Int"
            is BaseIntegerProperty -> propertyType = "Int"
            is BooleanProperty -> propertyType = "Boolean"
            is DoubleProperty -> propertyType = "Double"
            is DateTimeProperty -> propertyType = "java.time.ZonedDateTime"
            is ArrayProperty -> throw IllegalStateException("should not be called for ArrayProperty")
            is MapProperty -> throw IllegalStateException("should not be called for MapProperty")
            // TODO this is a fallback - not sure what type will actually work.
            is UntypedProperty -> propertyType = "String"
            is ObjectProperty -> {
                if (property.name == null) {
                    /*
                    Here we start compensating for missing definitions.

                    This is needed because CRD definitions do not always model every object to a dedicated model
                    in the spec.

                    This basically means they have types where they do not specify a name for.
                    Even if it is used in multiple places and even if the type exists in the standard platform types
                    they do not always reference them (not sure this is even possible).
                     */
                    propertyType = owningTypeName + owningAttributeName.capitalize()
                    if (!this.dslMeta.typeMeta.containsKey(DslTypeName(propertyType).absoluteName)) {
                        if (!this.additionalPropertyDefinitions.containsKey(propertyType)) {
                            val additionalDslClassInfo = DslClassInfoObjectPropertyAdapter.toDslClassInfo(property)
                            this.additionalPropertyDefinitions[propertyType] = additionalDslClassInfo
                        }
                    }
                }
            }
            else -> println("[WARN] unhandled property ${property.name} of type ${property.type}")
        }

        return propertyType
    }
}