package eu.rigeldev.kuberig.dsl.generator.meta.attributes

import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

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