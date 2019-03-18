package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindContainer

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
        KotlinClassWriter(typeName, this.classWriterProducer).use {classWriter ->

            classWriter.typeAnnotation("javax.annotation.Generated")
            classWriter.typeAnnotation("eu.rigeldev.kuberig.dsl.KubeRigDslMarker")

            classWriter.typeConstructorParameter(
                listOf("private", "val"),
                "sink",
                DslTypeName("eu.rigeldev.kuberig.dsl.DslResourceSink")
            )

            // dsl attributes and methods for sub containers
            container.subContainers.forEach { subName, subContainer ->
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
                        Pair("init","${subContainerType.typeShortName()}.() -> Unit")
                    ),
                    methodCode = listOf(
                        "this.$subName.init()"
                    ),
                    methodTypeDependencies = listOf(
                        subContainerType.absoluteName
                    )
                )
            }

            // dsl methods for kinds
            container.kinds.forEach { _, kindMeta ->
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
                        "this.sink.add(DslResource(alias, dsl.toValue()))"
                    ),
                    methodTypeDependencies = listOf(
                        "eu.rigeldev.kuberig.dsl.DslResource"
                    )
                )
            }

        }

        // generate sub containers
        container.subContainers.forEach { _, subContainer ->
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