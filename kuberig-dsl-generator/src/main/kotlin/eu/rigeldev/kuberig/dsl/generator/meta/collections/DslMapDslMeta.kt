package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta

class DslMapDslMeta(val type : DslTypeName,
                    val meta : DslMapAttributeMeta
) {

    fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_MapDsl")
    }

}