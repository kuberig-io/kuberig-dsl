package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

abstract class DslAttributeMeta(
    val name: String,
    val description: String,
    private val required: Boolean
) {

    fun isOptional(): Boolean {
        return !this.required
    }

    abstract fun absoluteAttributeDeclarationType() : DslTypeName

    abstract fun attributeDeclarationType() : String

    abstract fun toValueConstructorSuffix() : String
}

