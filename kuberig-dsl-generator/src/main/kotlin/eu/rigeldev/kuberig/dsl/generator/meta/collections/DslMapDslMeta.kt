package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta

class DslMapDslMeta(type : DslTypeName,
                    meta : DslMapAttributeMeta,
                    plural : Boolean
) :DslCollectionDslMeta<DslMapAttributeMeta>(type, meta, plural) {

    override fun itemType(): DslTypeName {
        return this.meta.itemType
    }

    override fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_MapDsl")
    }



}