package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.*
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslObjectAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindContainer
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.*
import eu.rigeldev.kuberig.dsl.generator.output.DslMetaConsumer
import java.io.BufferedWriter
import java.io.File
import java.util.*

class KotlinDslMetaConsumer(private val sourceOutputDirectory : File) : DslMetaConsumer {

    private lateinit var classWriterProducer : KotlinClassWriterProducer

    private lateinit var dslMeta : DslMeta

    private val listDslTypes = mutableListOf<DslListDslMeta>()
    private val mapDslTypes = mutableListOf<DslMapDslMeta>()

    override fun consume(dslMeta: DslMeta) {
        this.dslMeta = dslMeta

        this.classWriterProducer = KotlinClassWriterProducer(sourceOutputDirectory)

        dslMeta.typeMeta.forEach(this::generateTypeClass)
        dslMeta.typeMeta.forEach(this::generateTypeDslClass)
        dslMeta.kindMeta.forEach{ this.generateKindClass(it, dslMeta) }
        this.generateDslRoots(dslMeta)
        this.generateListDslTypes()
        this.generateMapDslTypes()
    }

    private fun classWriter(typeName : String) : BufferedWriter {
        return this.classWriterProducer.classWriter(typeName)
    }

    private fun generateListDslTypes() {
        val listDslTypeGenerator = KotlinListDslTypeGenerator(this.classWriterProducer)

        listDslTypes.forEach(listDslTypeGenerator::generateListDslType)
    }

    private fun generateMapDslTypes() {

        mapDslTypes.forEach { mapDslMeta ->

            val typeName = mapDslMeta.declarationType()

            val classWriter = this.classWriter(typeName.absoluteName)

            classWriter.use { writer ->
                writer.write("package ${mapDslMeta.type.packageName()}")
                writer.newLine()

                writer.newLine()

                writer.write("import javax.annotation.processing.Generated")
                writer.newLine()
                writer.write("import eu.rigeldev.kuberig.dsl.KubeRigDslMarker")
                writer.newLine()
                writer.write("import eu.rigeldev.kuberig.dsl.DslType")
                writer.newLine()
                if (mapDslMeta.meta.itemType.requiresImport()) {
                    writer.write("import ${mapDslMeta.meta.itemType.absoluteName}")
                    writer.newLine()
                }
                if (mapDslMeta.meta.itemType.requiresImport()) {
                    writer.write("import ${mapDslMeta.meta.itemType.absoluteName}Dsl")
                    writer.newLine()
                    writer.write("import java.util.stream.Collectors")
                    writer.newLine()
                } else {
                    writer.write("import java.util.Map.copyOf")
                    writer.newLine()
                }
                writer.write("import java.util.Collections")
                writer.newLine()


                writer.newLine()

                val resultMapItemValueType = mapDslMeta.meta.itemType.typeShortName()

                writer.write("@Generated")
                writer.newLine()
                writer.write("@KubeRigDslMarker")
                writer.newLine()
                writer.write("open class ${typeName.typeShortName()} : DslType<Map<String, $resultMapItemValueType>>  {")
                writer.newLine()

                val mapItemValueType = if (mapDslMeta.meta.itemType.requiresImport()) {
                    "${mapDslMeta.meta.itemType.typeShortName()}Dsl"
                } else {
                    mapDslMeta.meta.itemType.typeShortName()
                }

                writer.write("    private val map = mutableMapOf<String, $mapItemValueType>()")
                writer.newLine()

                writer.newLine()

                val addMethodName = if (mapDslMeta.meta.name.endsWith("s")) {
                    mapDslMeta.meta.name.substring(0, mapDslMeta.meta.name.length - 1)
                } else {
                    "item"
                }

                if (mapDslMeta.meta.itemType.requiresImport()) {
                    writer.write("    fun $addMethodName(${addMethodName}Key : String, init : ${mapItemValueType}.() -> Unit) { ")
                    writer.newLine()
                    writer.write("        val itemValue = $mapItemValueType()")
                    writer.newLine()
                    writer.write("        itemValue.init()")
                    writer.newLine()
                    writer.write("        this.map[${addMethodName}Key] = itemValue")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()

                    writer.newLine()

                    writer.write("    override fun toValue() : Map<String, $resultMapItemValueType> { ")
                    writer.newLine()
                    writer.write("        return Collections.unmodifiableMap(this.map.entries.stream()")
                    writer.newLine()
                    writer.write("            .collect(Collectors.toMap(")
                    writer.newLine()
                    writer.write("                { e -> e.key },")
                    writer.newLine()
                    writer.write("                { e -> e.value.toValue() }")
                    writer.newLine()
                    writer.write("            )))")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()

                } else {
                    writer.write("    fun $addMethodName(${addMethodName}Key : String, ${addMethodName}Value : $mapItemValueType) {")
                    writer.newLine()
                    writer.write("        this.map[${addMethodName}Key] = ${addMethodName}Value")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()

                    writer.newLine()

                    writer.write("    override fun toValue() : Map<String, $resultMapItemValueType> { ")
                    writer.newLine()
                    writer.write("        return copyOf(this.map)")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()
                }

                writer.write("}")
                writer.newLine()
            }


        }

    }

    private fun generateDslRoots(dslMeta : DslMeta) {

        println("generateDslRoots...")

        val kindsDslKindContainer = DslKindContainer("kinds")

        dslMeta.kindMeta.forEach {
            kindsDslKindContainer.add(it)
        }

        generateDslRootContainerClass("", kindsDslKindContainer)
    }

    fun generateDslRootContainerClass(parentPackage : String, container: DslKindContainer) {
        val absolutePackage = if (parentPackage == ""){
            container.name
        } else {
            "$parentPackage.${container.name}"
        }
        val typeName = "Dsl${container.name.capitalize()}Root"

        val absoluteName = "$absolutePackage.$typeName"

        val classWriter = this.classWriter(absoluteName)

        classWriter.use { writer ->
            // package section
            writer.write("package $absolutePackage")
            writer.newLine()
            // imports section
            writer.newLine()
            writer.write("import javax.annotation.processing.Generated")
            writer.newLine()
            writer.write("import eu.rigeldev.kuberig.dsl.KubeRigDslMarker")
            writer.newLine()
            writer.write("import eu.rigeldev.kuberig.dsl.DslResourceSink")
            writer.newLine()
            writer.write("import eu.rigeldev.kuberig.dsl.DslResource")
            writer.newLine()
            // import sub dsl roots
            container.subContainers.forEach { subName, subContainer ->
                writer.write("import $absolutePackage.$subName.${subContainer.typeName()}")
                writer.newLine()
            }
            writer.newLine()

            writer.write("@Generated")
            writer.newLine()
            writer.write("@KubeRigDslMarker")
            writer.newLine()
            writer.write("open class $typeName(private val sink : DslResourceSink) {")
            writer.newLine()

            // dsl attributes for sub containers
            writer.newLine()
            container.subContainers.forEach { subName, subContainer ->
                writer.write("    val $subName = ${subContainer.typeName()}(this.sink)")
                writer.newLine()
            }
            writer.newLine()

            // dsl methods for sub containers
            container.subContainers.forEach { subName, subContainer ->
                writer.write("    fun $subName(init : ${subContainer.typeName()}.() -> Unit) {")
                writer.newLine()
                writer.write("        this.$subName.init()")
                writer.newLine()
                writer.write("    }")
                writer.newLine()

                writer.newLine()
            }
            writer.newLine()

            // dsl methods for kinds
            container.kinds.forEach { _, kindMeta ->
                val kindType = kindMeta.kindType().typeShortName()
                val kindMethodName = kindMeta.methodName()

                writer.write("    fun $kindMethodName(alias : String, init : ${kindType}.() -> Unit) {")
                writer.newLine()
                writer.write("        val dsl = ${kindType}()")
                writer.newLine()
                writer.write("        dsl.init()")
                writer.newLine()
                writer.write("        this.sink.add(DslResource(alias, dsl.toValue()))")
                writer.newLine()
                writer.write("    }")
                writer.newLine()

                writer.newLine()
            }

            writer.write("}")
            writer.newLine()
        }

        // generate sub containers
        container.subContainers.forEach { _, subContainer ->
            this.generateDslRootContainerClass(absolutePackage, subContainer)
        }
    }

    fun generateKindClass(kindMeta : DslKindMeta, dslMeta : DslMeta) {


        val kindTypeName = kindMeta.kindType()

        val packageName = kindTypeName.packageName()
        val kindType = kindTypeName.typeShortName()


        println("Generating [KIND] $packageName.${kindMeta.kind} of [TYPE] ${kindMeta.typeName.absoluteName}...")

        val absoluteName = packageName + "." + kindMeta.kind + "Dsl"
        val typeMeta = dslMeta.typeMeta[kindMeta.typeName.absoluteName] ?: throw IllegalStateException("No type meta for $packageName.${kindMeta.kind}")

        val classWriter = this.classWriter(absoluteName)
        classWriter.use { writer ->

            writer.write("package $packageName")
            writer.newLine()

            this.writeImports(writer, typeMeta, "Dsl")
            writer.write("import ${typeMeta.absoluteName}")
            writer.newLine()

            if (typeMeta is DslObjectTypeMeta) {

                typeMeta.attributes.minus("status").entries.forEach { attributeEntry ->
                    val attributeMeta = attributeEntry.value

                    if (attributeMeta is DslListAttributeMeta) {
                        val listDslMeta = DslListDslMeta(
                            DslTypeName(typeMeta.absoluteName), attributeMeta
                        )

                        writer.write("import ${listDslMeta.declarationType().absoluteName}")
                        writer.newLine()
                        listDslTypes.add(listDslMeta)
                        attributeMeta.listDslMeta = listDslMeta
                    }
                    else if (attributeMeta is DslMapAttributeMeta) {
                        val mapDslMeta = DslMapDslMeta(
                            DslTypeName(typeMeta.absoluteName), attributeMeta
                        )
                        writer.write("import ${mapDslMeta.declarationType().absoluteName}")
                        writer.newLine()
                        mapDslTypes.add(mapDslMeta)
                        attributeMeta.mapDslMeta = mapDslMeta
                    }
                }

            }

            writer.newLine()

            writeDoc(writer, typeMeta.description)
            writer.write("@Generated")
            writer.newLine()
            writer.write("@KubeRigDslMarker")
            writer.newLine()
            writer.write("open class $kindType : DslType<${typeMeta.name}> {")
            writer.newLine()

            writer.newLine()

            if (typeMeta is DslObjectTypeMeta) {

                val attributeIterator = typeMeta.attributes.minus("status").entries.iterator()

                while (attributeIterator.hasNext()) {
                    val (attributeName, attributeMeta) = attributeIterator.next()

                    if (attributeMeta is DslObjectAttributeMeta && attributeMeta.absoluteType.requiresImport()) {
                        writer.write("    private var $attributeName : ${attributeMeta.attributeDeclarationType()}Dsl? = null")
                    }
                    else if (attributeMeta is DslListAttributeMeta) {
                        val listDslMeta = attributeMeta.listDslMeta!!
                        writer.write("    private val $attributeName = ${listDslMeta.declarationType().typeShortName()}()")
                        listDslTypes.add(listDslMeta)
                    }
                    else if (attributeMeta is DslMapAttributeMeta) {
                        val mapDslMeta = attributeMeta.mapDslMeta!!
                        writer.write("    private val $attributeName = ${mapDslMeta.declarationType().typeShortName()}()")
                    }
                    else {
                        this.writeDoc(writer, attributeMeta.description, "    ")
                        writer.write("    var $attributeName : ${attributeMeta.attributeDeclarationType()}")
                        writer.write("? = null")
                    }

                    writer.newLine()
                }

                writer.newLine()


                typeMeta.attributes.minus(listOf("kind", "apiVersion", "status")).forEach { attributeName, attributeMeta ->

                    this.writeDoc(writer, attributeMeta.description, "    ")

                    if (attributeMeta is DslObjectAttributeMeta && attributeMeta.absoluteType.requiresImport()) {
                        writer.write("    fun $attributeName(init : ${attributeMeta.attributeDeclarationType()}Dsl.() -> Unit) {")
                        writer.newLine()
                        writer.write("        val attr = ${attributeMeta.attributeDeclarationType()}Dsl()")
                        writer.newLine()
                        writer.write("        attr.init()")
                        writer.newLine()
                        writer.write("        this.$attributeName = attr")
                        writer.newLine()
                        writer.write("    }")
                        writer.newLine()
                    }
                    else if (attributeMeta is DslListAttributeMeta) {
                        val listDslMeta = attributeMeta.listDslMeta!!

                        writer.write("    fun $attributeName(init: ${listDslMeta.declarationType().typeShortName()}.() -> Unit) {")
                        writer.newLine()
                        writer.write("        this.$attributeName.init()")
                        writer.newLine()
                        writer.write("    }")
                        writer.newLine()

                    }
                    else if (attributeMeta is DslMapAttributeMeta) {
                        val mapDslMeta = attributeMeta.mapDslMeta!!

                        writer.write("    fun $attributeName(init: ${mapDslMeta.declarationType().typeShortName()}.() -> Unit) {")
                        writer.newLine()
                        writer.write("        this.$attributeName.init()")
                        writer.newLine()
                        writer.write("    }")
                        writer.newLine()
                    }
                    else {

                        writer.write("    fun $attributeName(")
                        writer.write("$attributeName : ${attributeMeta.attributeDeclarationType()}")
                        writer.write(") { ")
                        writer.newLine()

                        writer.write("        this.$attributeName = $attributeName")
                        writer.newLine()

                        writer.write("    }")
                        writer.newLine()
                    }

                    writer.newLine()
                }
                writer.newLine()

                writer.write("    override fun toValue() : ${typeMeta.name} {")
                writer.newLine()

                writer.write("        return ${typeMeta.name}(")
                writer.newLine()
                val constructorIt = typeMeta.attributes.minus("status").iterator()
                while(constructorIt.hasNext()) {
                    val (attributeName, attributeMeta) = constructorIt.next()

                    val attributeValueSuffix = if (attributeMeta is DslObjectAttributeMeta && !attributeMeta.absoluteType.requiresImport()) {
                        ""
                    } else if (attributeMeta is DslListAttributeMeta) {
                        ".toValue()"
                    } else if (attributeMeta is DslMapAttributeMeta) {
                        ".toValue()"
                    } else {
                        "?.toValue()"
                    }

                    writer.write("            ${attributeName} = ")
                    when (attributeName) {
                        "kind" -> writer.write("\"${kindMeta.kind}\"")
                        "apiVersion" -> {
                            if (kindMeta.group != "") {
                                writer.write("\"${kindMeta.group}/")
                            } else {
                                writer.write("\"")
                            }
                            writer.write("${kindMeta.version}\"")
                        }
                        else -> writer.write("this.${attributeName}$attributeValueSuffix")
                    }

                    if (constructorIt.hasNext()) {
                        writer.write(",")
                        writer.newLine()
                    }

                }

                writer.newLine()
                writer.write("        )")
                writer.newLine()

                writer.write("    }")
                writer.newLine()

            }

            writer.write("}")
            writer.newLine()

            writer.newLine()
        }
    }

    fun generateTypeDslClass(name : String, typeMeta : DslTypeMeta) {

        println("Generating [TYPE] ${name}...")

        val classWriter = this.classWriter(typeMeta.absoluteName + "Dsl")
        classWriter.use { writer ->
            if (typeMeta.packageName != "") {
                writer.write("package ${typeMeta.packageName}")
                writer.newLine()
            }

            writer.newLine()

            this.writeImports(writer, typeMeta, "Dsl")

            if (typeMeta is DslObjectTypeMeta) {

                typeMeta.attributes.minus("status").entries.forEach { attributeEntry ->
                    val attributeMeta = attributeEntry.value

                    if (attributeMeta is DslListAttributeMeta) {
                        val listDslMeta = DslListDslMeta(
                            DslTypeName(typeMeta.absoluteName), attributeMeta
                        )

                        writer.write("import ${listDslMeta.declarationType().absoluteName}")
                        writer.newLine()
                        listDslTypes.add(listDslMeta)
                        attributeMeta.listDslMeta = listDslMeta
                    }
                    else if (attributeMeta is DslMapAttributeMeta) {
                        val mapDslMeta = DslMapDslMeta(
                            DslTypeName(typeMeta.absoluteName), attributeMeta
                        )
                        writer.write("import ${mapDslMeta.declarationType().absoluteName}")
                        writer.newLine()
                        mapDslTypes.add(mapDslMeta)
                        attributeMeta.mapDslMeta = mapDslMeta
                    }
                }

            }

            writer.newLine()

            this.writeDoc(writer, typeMeta.description)



            when (typeMeta) {
                is DslObjectTypeMeta -> {
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@KubeRigDslMarker")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name}Dsl : DslType<${typeMeta.name}> {")
                    writer.newLine()

                    writer.newLine()


                    val attributeIterator = typeMeta.attributes.minus("status").entries.iterator()

                    while (attributeIterator.hasNext()) {
                        val (attributeName, attributeMeta) = attributeIterator.next()


                        if (attributeMeta is DslObjectAttributeMeta && attributeMeta.absoluteType.requiresImport()) {
                            writer.write("    private var $attributeName : ${attributeMeta.attributeDeclarationType()}Dsl? = null")
                        }
                        else if (attributeMeta is DslListAttributeMeta) {
                            val listDslMeta = attributeMeta.listDslMeta!!
                            writer.write("    private val $attributeName = ${listDslMeta.declarationType().typeShortName()}()")
                        }
                        else if (attributeMeta is DslMapAttributeMeta) {
                            val mapDslMeta = attributeMeta.mapDslMeta!!
                            writer.write("    private val $attributeName = ${mapDslMeta.declarationType().typeShortName()}()")
                        }
                        else {
                            this.writeDoc(writer, attributeMeta.description, "    ")
                            writer.write("    var $attributeName : ${attributeMeta.attributeDeclarationType()}")
                            writer.write("? = null")
                        }

                        writer.newLine()
                    }

                    writer.newLine()


                    typeMeta.attributes.minus(listOf("kind", "apiVersion", "status")).forEach { attributeName, attributeMeta ->
                        this.writeDoc(writer, attributeMeta.description, "    ")

                        if (attributeMeta is DslObjectAttributeMeta && attributeMeta.absoluteType.requiresImport()) {
                            writer.write("    fun $attributeName(init : ${attributeMeta.attributeDeclarationType()}Dsl.() -> Unit) {")
                            writer.newLine()
                            writer.write("        val attr = ${attributeMeta.attributeDeclarationType()}Dsl()")
                            writer.newLine()
                            writer.write("        attr.init()")
                            writer.newLine()
                            writer.write("        this.$attributeName = attr")
                            writer.newLine()
                            writer.write("    }")
                            writer.newLine()
                        }
                        else if (attributeMeta is DslListAttributeMeta) {
                            val listDslMeta = attributeMeta.listDslMeta!!

                            writer.write("    fun $attributeName(init: ${listDslMeta.declarationType().typeShortName()}.() -> Unit) {")
                            writer.newLine()
                            writer.write("        this.$attributeName.init()")
                            writer.newLine()
                            writer.write("    }")
                            writer.newLine()

                        }
                        else if (attributeMeta is DslMapAttributeMeta) {
                            val mapDslMeta = attributeMeta.mapDslMeta!!

                            writer.write("    fun $attributeName(init: ${mapDslMeta.declarationType().typeShortName()}.() -> Unit) {")
                            writer.newLine()
                            writer.write("        this.$attributeName.init()")
                            writer.newLine()
                            writer.write("    }")
                            writer.newLine()
                        }
                        else {

                            writer.write("    fun $attributeName(")
                            writer.write("$attributeName : ${attributeMeta.attributeDeclarationType()}")
                            writer.write(") { ")
                            writer.newLine()

                            writer.write("        this.$attributeName = $attributeName")
                            writer.newLine()

                            writer.write("    }")
                            writer.newLine()
                        }

                        writer.newLine()
                    }
                    writer.newLine()


                    writer.write("    override fun toValue() : ${typeMeta.name} { ")
                    writer.newLine()

                    writer.write("        return ${typeMeta.name}(")
                    writer.newLine()
                    val constructorIt = typeMeta.attributes.minus("status").iterator()
                    while(constructorIt.hasNext()) {
                        val (attributeName, attributeMeta) = constructorIt.next()

                        val attributeValueSuffix = if (attributeMeta is DslObjectAttributeMeta && !attributeMeta.absoluteType.requiresImport()) {
                            ""
                        } else if (attributeMeta is DslListAttributeMeta) {
                            ".toValue()"
                        } else if (attributeMeta is DslMapAttributeMeta) {
                            ".toValue()"
                        } else {
                            "?.toValue()"
                        }

                        writer.write("            $attributeName = this.$attributeName$attributeValueSuffix")

                        if (constructorIt.hasNext()) {
                            writer.write(",")
                            writer.newLine()
                        }

                    }

                    writer.newLine()
                    writer.write("        )")
                    writer.newLine()

                    writer.write("    }")
                    writer.newLine()

                    writer.write("}")
                    writer.newLine()

                }
                is DslContainerTypeMeta -> {
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@KubeRigDslMarker")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name}Dsl : DslType<${typeMeta.name}> { ")
                    writer.newLine()

                    writer.write("    var value : ${typeMeta.containedType.typeShortName()}? = null")
                    writer.newLine()

                    writer.newLine()

                    if (typeMeta.containedType.requiresImport() && this.dslMeta.isPlatformApiType(typeMeta.containedType)) {
                        writer.write("    fun ${typeMeta.name.toLowerCase()}(init : ${typeMeta.containedType.typeShortName()}.() -> Unit) : ${typeMeta.containedType.typeShortName()} {")
                        writer.newLine()
                        writer.write("        val attr = ${typeMeta.containedType.typeShortName()}()")
                        writer.newLine()
                        writer.write("        attr.init()")
                        writer.newLine()
                        writer.write("        this.value = attr")
                        writer.newLine()
                        writer.write("        return attr")
                        writer.newLine()
                        writer.write("    }")
                        writer.newLine()
                    } else {
                        writer.write("    fun ${typeMeta.name.toLowerCase()}(")
                        writer.write("value : ${typeMeta.containedType.typeShortName()}")
                        writer.write(") { ")
                        writer.newLine()

                        writer.write("        this.value = value")
                        writer.newLine()

                        writer.write("    }")
                        writer.newLine()
                    }

                    writer.write("    override fun toValue() : ${typeMeta.name} { ")
                    writer.newLine()
                    writer.write("        return ${typeMeta.name}(this.value!!)")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()

                    writer.write("}")
                    writer.newLine()
                }
                is DslSealedTypeMeta -> {
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@KubeRigDslMarker")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name}Dsl : DslType<${typeMeta.name}> { ")
                    writer.newLine()
                    writer.write("    private var value : ${typeMeta.name}? = null")
                    writer.newLine()

                    writer.newLine()

                    typeMeta.sealedTypes.forEach { name, typeName ->
                        writer.write("    fun value(value : ${typeName.typeShortName()}) {")
                        writer.newLine()
                        writer.write("        this.value = ${typeMeta.name}_$name(value)")
                        writer.newLine()
                        writer.write("    }")
                        writer.newLine()

                        writer.newLine()

                    }

                    writer.write("    override fun toValue() : ${typeMeta.name} { ")
                    writer.newLine()
                    writer.write("        return this.value!!")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()

                    writer.write("}")
                    writer.newLine()

                }
                is DslInterfaceTypeMeta -> {
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@KubeRigDslMarker")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name}Dsl : DslType<${typeMeta.name}> { ")
                    writer.newLine()
                    writer.write("    override fun toValue() : ${typeMeta.name} { ")
                    writer.newLine()
                    writer.write("        return ${typeMeta.name}()")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()
                    writer.write("}")
                    writer.newLine()

                }
                else -> throw IllegalStateException("Don't know what to do with " + typeMeta::javaClass)
            }

            writer.newLine()
        }

    }

    fun generateTypeClass(name : String, typeMeta: DslTypeMeta) {

        println("Generating [TYPE] ${name}...")

        val classWriter = this.classWriter(typeMeta.absoluteName)
        classWriter.use { writer ->
            if (typeMeta.packageName != "") {
                writer.write("package ${typeMeta.packageName}")
                writer.newLine()
            }

            writer.newLine()

            this.writeImports(writer, typeMeta, "")
            if (typeMeta is DslSealedTypeMeta || typeMeta is DslContainerTypeMeta) {
                writer.write("import com.fasterxml.jackson.core.JsonGenerator")
                writer.newLine()
                writer.write("import com.fasterxml.jackson.databind.JsonSerializer")
                writer.newLine()
                writer.write("import com.fasterxml.jackson.databind.SerializerProvider")
                writer.newLine()
                writer.write("import com.fasterxml.jackson.databind.annotation.JsonSerialize")
                writer.newLine()
            }

            this.writeDoc(writer, typeMeta.description)



            when (typeMeta) {
                is DslObjectTypeMeta -> {
                    writeDoc(writer, typeMeta.description)
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name} (")
                    writer.newLine()


                    val attributeIterator = typeMeta.attributes.minus("status").entries.iterator()

                    while (attributeIterator.hasNext()) {
                        val (attributeName, attributeMeta) = attributeIterator.next()

                        this.writeDoc(writer, attributeMeta.description, "    ")

                        writer.write("    val $attributeName : ${attributeMeta.attributeDeclarationType()}")
                        if (attributeMeta.isOptional()) {
                            writer.write("?")
                        }

                        if (attributeIterator.hasNext()) {
                            writer.write(", ")
                            writer.newLine()
                        }
                    }

                    writer.write(")")
                    writer.newLine()

                }
                is DslContainerTypeMeta -> {
                    writeDoc(writer, typeMeta.description)
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@JsonSerialize(using = ${typeMeta.name}_Serializer::class)")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name} (val value : ${typeMeta.containedType.typeShortName()})")
                    writer.newLine()

                    writer.newLine()

                    writer.write("class ${typeMeta.name}_Serializer : JsonSerializer<${typeMeta.name}>() {")
                    writer.newLine()
                    writer.write("    override fun serialize(value: ${typeMeta.name}?, gen: JsonGenerator?, serializers: SerializerProvider?) {")
                    writer.newLine()
                    val writeMethod = when {
                        "Int" == typeMeta.containedType.typeShortName() -> "writeNumber"
                        "String" == typeMeta.containedType.typeShortName() -> "writeString"
                        else -> "writeObject"
                    }

                    writer.write("        gen!!.$writeMethod(value?.value)")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()
                    writer.write("}")
                    writer.newLine()

                }
                is DslSealedTypeMeta -> {
                    writeDoc(writer, typeMeta.description)
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("@JsonSerialize(using = ${typeMeta.name}_Serializer::class)")
                    writer.newLine()
                    writer.write("sealed class ${typeMeta.name}")
                    writer.newLine()

                    writer.newLine()

                    typeMeta.sealedTypes.forEach { name, typeName ->
                        writer.write("@Generated")
                        writer.newLine()
                        writer.write("class ${typeMeta.name}")
                        writer.write("_$name (val value : ${typeName.typeShortName()}) : ${typeMeta.name}()")
                        writer.newLine()

                        writer.newLine()

                    }

                    writer.write("class ${typeMeta.name}_Serializer : JsonSerializer<${typeMeta.name}>() {")
                    writer.newLine()
                    writer.write("    override fun serialize(value: ${typeMeta.name}?, gen: JsonGenerator?, serializers: SerializerProvider?) {")
                    writer.newLine()
                    writer.write("        when (value) {")
                    writer.newLine()
                    typeMeta.sealedTypes.forEach { name, typeName ->
                        val writeMethod = when {
                            "Int" == typeName.typeShortName() -> "writeNumber"
                            "String" == typeName.typeShortName() -> "writeString"
                            else -> "writeObject"
                        }


                        writer.write("            is ${typeMeta.name}_$name -> gen!!.$writeMethod(value.value)")
                        writer.newLine()
                    }
                    writer.write("        } ")
                    writer.newLine()
                    writer.write("    }")
                    writer.newLine()
                    writer.write("}")
                    writer.newLine()

                }
                is DslInterfaceTypeMeta -> {
                    writeDoc(writer, typeMeta.description)
                    writer.write("@Generated")
                    writer.newLine()
                    writer.write("open class ${typeMeta.name}")
                    writer.newLine()

                }
                else -> throw IllegalStateException("Don't know what to do with " + typeMeta::javaClass)
            }

            writer.newLine()
        }

    }

    private fun writeImports(writer: BufferedWriter, typeMeta: DslTypeMeta, suffix : String) {
        val typesToImport = mutableSetOf<String>()

        typesToImport.add("javax.annotation.processing.Generated")
        typesToImport.add("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")
        typesToImport.add("eu.rigeldev.kuberig.dsl.DslType")

        typeMeta.typeDependencies.forEach {
            if (this.dslMeta.isPlatformApiType(it.absoluteName)) {
                typesToImport.add("${it.absoluteName}$suffix")
            }

            typesToImport.add(it.absoluteName)
        }

        typesToImport.forEach {
            writer.write("import $it")
            writer.newLine()
        }

        writer.newLine()
    }

    private fun writeDoc(writer : BufferedWriter, documentation : String, prefix : String = "") {
        if (documentation != "") {
            writer.write(prefix)
            writer.write("/**")
            writer.newLine()

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
                    writer.write(lineBuffer.toString())
                    writer.newLine()

                    lineBuffer = StringBuffer()
                    lineBuffer.append(prefix)
                    lineBuffer.append(" *")
                }

                lineBuffer.append(" ")
                lineBuffer.append(currentDocSplit)

            }

            writer.write(lineBuffer.toString())
            writer.newLine()

            writer.write(prefix)
            writer.write(" **/")
            writer.newLine()
        }
    }
}