package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslObjectAttributeMeta(
    name: String,
    description: String,
    required: Boolean,
    val absoluteType: DslTypeName
) : DslAttributeMeta(name, description, required) {

    override fun attributeDeclarationType(): String {
        return this.absoluteType.typeShortName()
    }
}