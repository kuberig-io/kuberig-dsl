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

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return this.absoluteType
    }

    override fun toValueConstructorSuffix(): String {
        return if (this.absoluteType.requiresImport()) {
            return if (super.isOptional() && !listOf("metadata", "spec").contains(this.name)) {
                "?.toValue()"
            } else {
                "!!.toValue()"
            }
        } else {
            ""
        }
    }
}