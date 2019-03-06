package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta

class DslListDslMeta(type : DslTypeName,
                     meta : DslListAttributeMeta,
                     plural : Boolean
) :DslCollectionDslMeta<DslListAttributeMeta>(type, meta, plural) {

    override fun itemType(): DslTypeName {
        return this.meta.itemType
    }

    override fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_ListDsl")
    }

}