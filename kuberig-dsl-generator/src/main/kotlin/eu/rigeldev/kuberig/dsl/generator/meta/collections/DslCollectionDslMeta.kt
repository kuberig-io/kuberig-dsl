package eu.rigeldev.kuberig.dsl.generator.meta.collections

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslAttributeMeta

abstract class DslCollectionDslMeta<AM : DslAttributeMeta>(val type : DslTypeName,
                                                           val meta : AM,
                                                           val plural : Boolean) {

    abstract fun declarationType() : DslTypeName

    abstract fun itemType() : DslTypeName

    fun complexItemType() : Boolean {
        return this.itemType().requiresImport()
    }

    private fun itemTypeSuffix() : String {
        return if (this.complexItemType()) {
            "Dsl"
        } else {
            ""
        }
    }

    fun dslItemType() : DslTypeName {
        return DslTypeName(this.itemType().absoluteName + this.itemTypeSuffix())
    }

    /**
     * Check if attribute name this list is for is plural
     * most likely this does not cover all non-plural cases (to-be-verified).
     */
    fun addMethodName() : String {
        return if (this.plural) {
            meta.name.substring(0, meta.name.length - 1)
        } else {
            "item"
        }
    }
}