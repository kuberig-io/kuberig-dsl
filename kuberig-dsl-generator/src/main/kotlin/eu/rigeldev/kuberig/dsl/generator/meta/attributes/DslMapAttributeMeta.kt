package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslMapAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    private val keyType : DslTypeName,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {
    var mapDslMeta : DslMapDslMeta? = null

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return DslTypeName("Map")
    }

    override fun attributeDeclarationType(): String {
        return "Map<${keyType.typeShortName()}, ${itemType.typeShortName()}>"
    }

    override fun toValueConstructorSuffix(): String {
        return ".toValue()"
    }
}