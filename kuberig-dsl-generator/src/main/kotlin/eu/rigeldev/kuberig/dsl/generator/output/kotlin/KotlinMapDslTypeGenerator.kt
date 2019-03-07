package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta

class KotlinMapDslTypeGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    fun generateMapDslType(mapDslMeta: DslMapDslMeta) {
        KotlinClassWriter(mapDslMeta.declarationType(), this.classWriterProducer).use { classWriter ->

            val resultMapItemType = mapDslMeta.meta.itemType.typeShortName()

            classWriter.typeAnnotation("javax.annotation.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<Map<String, $resultMapItemType>>",
                listOf("eu.rigeldev.kuberig.dsl.DslType", mapDslMeta.meta.itemType.absoluteName)
            )

            val mapItemType = mapDslMeta.dslItemType().typeShortName()

            classWriter.mapTypeAttribute(
                listOf("private", "val"),
                "map",
                DslTypeName("MutableMap"),
                DslTypeName("String"),
                DslTypeName(mapDslMeta.dslItemType().absoluteName),
                "mutableMapOf()"
            )

            val addMethodName = mapDslMeta.addMethodName()

            if (mapDslMeta.complexItemType()) {
                classWriter.typeMethod(
                    methodName = addMethodName,
                    methodParameters = "key : String, init : $mapItemType.() -> Unit",
                    methodCode = listOf(
                        "val itemValue = $mapItemType()",
                        "itemValue.init()",
                        "this.map[key] = itemValue"
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
                    methodParameters = "key : String, value : $mapItemType",
                    methodCode = listOf(
                        "this.map[key] = value"
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