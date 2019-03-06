package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class DslListAttributeMeta(
    name : String,
    description : String,
    required : Boolean,
    val itemType : DslTypeName
) : DslAttributeMeta(name, description, required) {

    override fun absoluteAttributeDeclarationType(): DslTypeName {
        return DslTypeName("List")
    }

    override fun attributeDeclarationType(): String {
        return "List<${itemType.typeShortName()}>"
    }

    override fun toValueConstructorSuffix(): String {
        return ".toValue()"
    }
}