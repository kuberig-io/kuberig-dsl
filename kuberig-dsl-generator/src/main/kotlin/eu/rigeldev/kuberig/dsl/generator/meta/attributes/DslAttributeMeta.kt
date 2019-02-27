package eu.rigeldev.kuberig.dsl.generator.meta.attributes

abstract class DslAttributeMeta(
    val name: String,
    val description: String,
    private val required: Boolean
) {

    fun isOptional(): Boolean {
        return !this.required
    }

    abstract fun attributeDeclarationType() : String
}

