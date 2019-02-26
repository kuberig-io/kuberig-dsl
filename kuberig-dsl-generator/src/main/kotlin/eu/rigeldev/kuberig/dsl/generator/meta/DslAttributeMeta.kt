package eu.rigeldev.kuberig.dsl.generator.meta

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

class DslListAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {

    var listDslMeta : DslListDslMeta? = null

    override fun attributeDeclarationType(): String {
        return "List<${itemType.typeShortName()}>"
    }
}

class DslMapAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    private val keyType : DslTypeName,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {

    var mapDslMeta : DslMapDslMeta? = null

    override fun attributeDeclarationType(): String {
        return "Map<${keyType.typeShortName()}, ${itemType.typeShortName()}>"
    }
}