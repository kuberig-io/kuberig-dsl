package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta

class KotlinListDslTypeGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    fun generateListDslType(listDslMeta: DslListDslMeta) {
        KotlinClassWriter(listDslMeta.declarationType(), this.classWriterProducer).use { classWriter ->
            val resultListItemType = listDslMeta.meta.itemType.typeShortName()

            classWriter.typeAnnotation("javax.annotation.processing.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeInterface(
                "DslType<List<$resultListItemType>>",
                listOf("eu.rigeldev.kuberig.dsl.DslType", listDslMeta.meta.itemType.absoluteName)
            )

            val listItemType = listDslMeta.dslItemType().typeShortName()

            classWriter.listTypeAttribute(
                listOf("private", "val"),
                "list",
                DslTypeName("MutableList"),
                DslTypeName(listDslMeta.dslItemType().absoluteName),
                "mutableListOf()"
            )

            val addMethodName = listDslMeta.addMethodName()

            if (listDslMeta.complexItemType()) {
                classWriter.typeMethod(
                    methodName = addMethodName,
                    methodParameters = "init : $listItemType.() -> Unit",
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
                    methodName = addMethodName,
                    methodParameters = "$addMethodName : $listItemType",
                    methodCode = listOf(
                        "this.list.add($addMethodName)"
                    )
                )

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
        }
    }
}