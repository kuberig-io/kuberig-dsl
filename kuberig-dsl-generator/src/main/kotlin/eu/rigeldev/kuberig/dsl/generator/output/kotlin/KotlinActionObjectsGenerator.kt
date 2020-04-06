package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.Kind

/**
 * Generate action info objects and KindActionRegister object.
 */
class KotlinActionObjectsGenerator(
        private val dslMeta: DslMeta,
        private val classWriterProducer: KotlinClassWriterProducer) {

    fun generateActionObjects() {

        this.generateKindActionObjects()
        this.generateKindActionRegister()
    }

    private fun actionsType(kind: Kind): DslTypeName {
        var typeName = "kinds."

        if (kind.group != "") {
            typeName += kind.group + "."
        }

        typeName += kind.version + "."
        typeName += kind.kind + "Actions"

        return DslTypeName(typeName)
    }

    private fun generateKindActionObjects() {

        this.dslMeta.kindApiActions.values.forEach {

            val actionsType = this.actionsType(it.kind)

            val kotlinClassWriter = KotlinClassWriter(
                    actionsType,
                    this.classWriterProducer,
                    "object"
            )

            kotlinClassWriter.use { classWriter ->
                classWriter.typeAttribute(
                        modifiers = listOf("val"),
                        attributeName = "id",
                        declarationType = DslTypeName("eu.rigeldev.kuberig.dsl.actions.KindId"),
                        defaultValue = "KindId(\"${it.kind.apiVersion()}\",\"${it.kind.kind}\")"
                )

                classWriter.typeAttribute(
                        modifiers = listOf("val"),
                        attributeName = "actions",
                        declarationType = DslTypeName("eu.rigeldev.kuberig.dsl.actions.KindActions"),
                        defaultValue = "KindActions(id, listOf())"
                )

                classWriter.typeMethod(
                        methodName = "registrationPair",
                        methodReturnType = "Pair<KindId, KindActions>",
                        methodCode = listOf(
                                "return Pair(id, actions)"
                        )
                )
            }

        }

    }

    private fun generateKindActionRegister() {
        val kotlinClassWriter = KotlinClassWriter(
                DslTypeName("kinds.KindActionRegister"),
                this.classWriterProducer,
                "object"
        )



        kotlinClassWriter.use {classWriter ->

            var kindActionsValue = "mapOf(\n"

            val kindActionsIterator = this.dslMeta.kindApiActions.values.iterator()

            while (kindActionsIterator.hasNext()) {
                val kindActions = kindActionsIterator.next()

                val actionsType = this.actionsType(kindActions.kind)

                kindActionsValue += "\t\t\t${actionsType.absoluteName}.registrationPair()"


                if (kindActionsIterator.hasNext()) {
                    kindActionsValue += ",\n"
                }
            }

            kindActionsValue += "\t\t)"

            classWriter.mapTypeAttribute(
                    modifiers = listOf("private val"),
                    attributeName = "kindActions",
                    declarationType = DslTypeName("Map"),
                    declarationKeyType = DslTypeName("eu.rigeldev.kuberig.dsl.actions.KindId"),
                    declarationItemType = DslTypeName("eu.rigeldev.kuberig.dsl.actions.KindActions"),
                    defaultValue = kindActionsValue
            )


            classWriter.typeMethod(
                    methodName = "actions",
                    methodParameters = listOf(
                            Pair("apiVersion", "String"),
                            Pair("kind", "String")
                    ),
                    methodReturnType = "KindActions?",
                    methodCode = listOf("return kindActions[KindId(apiVersion, kind)]"),
                    methodTypeDependencies = listOf(
                            "eu.rigeldev.kuberig.dsl.actions.KindActions",
                            "eu.rigeldev.kuberig.dsl.actions.KindId"
                    )
            )
        }
    }

}
