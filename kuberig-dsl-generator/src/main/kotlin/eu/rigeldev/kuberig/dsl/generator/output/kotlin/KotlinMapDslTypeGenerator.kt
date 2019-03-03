package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta

class KotlinMapDslTypeGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    fun generateMapDslType(mapDslMeta: DslMapDslMeta) {
        KotlinClassWriter(mapDslMeta.declarationType(), this.classWriterProducer).use { classWriter ->

            val resultMapItemType = mapDslMeta.meta.itemType.typeShortName()

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<Map<String, $resultMapItemType>>",
                listOf("eu.rigeldev.kuberig.dsl.DslType", mapDslMeta.meta.itemType.absoluteName)
            )

            val complexType = mapDslMeta.meta.itemType.requiresImport()

            val mapItemTypeSuffix = if (complexType) {
                "Dsl"
            } else {
                ""
            }

            val mapItemType = "${mapDslMeta.meta.itemType.typeShortName()}$mapItemTypeSuffix"

            classWriter.mapTypeAttribute(
                listOf("private", "val"),
                "map",
                DslTypeName("MutableMap"),
                DslTypeName("String"),
                DslTypeName(mapDslMeta.meta.itemType.absoluteName + mapItemTypeSuffix),
                "mutableMapOf()"
            )

            val addMethodName = if (mapDslMeta.meta.name.endsWith("s")) {
                mapDslMeta.meta.name.substring(0, mapDslMeta.meta.name.length - 1)
            } else {
                "item"
            }

            if (complexType) {
                classWriter.typeMethod(
                    methodName = addMethodName,
                    methodParameters = "${addMethodName}Key : String, init : $mapItemType.() -> Unit",
                    methodCode = listOf(
                        "val itemValue = $mapItemType()",
                        "itemValue.init()",
                        "this.map[${addMethodName}Key] = itemValue"
                    )
                )

                classWriter.typeMethod(
                    modifiers = listOf("override"),
                    methodName = "toValue",
                    methodReturnType = "Map<String, $resultMapItemType>",
                    methodCode = listOf(
                        "return Collections.unmodifiableMap(this.map.entries.stream()",
                        "    .collect(Collectors.toMap(",
                        "        { e -> e.key },",
                        "        { e -> e.value.toValue() }",
                        "    )))"
                    ),
                    methodTypeDependencies = listOf(
                        "java.util.stream.Collectors",
                        "java.util.Collections"
                    )
                )
            }
            else {
                classWriter.typeMethod(
                    methodName = addMethodName,
                    methodParameters = "${addMethodName}Key : String, ${addMethodName}Value : $mapItemType",
                    methodCode = listOf(
                        "this.map[${addMethodName}Key] = ${addMethodName}Value"
                    )
                )

                classWriter.typeMethod(
                    modifiers = listOf("override"),
                    methodName = "toValue",
                    methodReturnType = "Map<String, $resultMapItemType>",
                    methodCode = listOf(
                        "return Collections.unmodifiableMap(this.map)"
                    ),
                    methodTypeDependencies = listOf(
                        "java.util.Collections"
                    )
                )
            }
        }
    }

}