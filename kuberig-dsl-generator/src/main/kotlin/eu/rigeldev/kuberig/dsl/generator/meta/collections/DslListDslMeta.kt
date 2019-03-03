package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta

class DslListDslMeta(val type : DslTypeName,
                     val meta : DslListAttributeMeta
) {

    fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_ListDsl")
    }

}