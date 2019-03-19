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

    fun getterMethodName() : String {
        val splitIndex = 1

        val wrappingNeeded = name.startsWith('$')

        val methodName = "get" + this.name.substring(0, splitIndex).toUpperCase() + this.name.substring(splitIndex)

        return if (wrappingNeeded) {
            "`$methodName`"
        } else {
            methodName
        }
    }

    abstract fun absoluteAttributeDeclarationType() : DslTypeName

    abstract fun attributeDeclarationType() : String

    abstract fun toValueConstructorSuffix() : String
}

