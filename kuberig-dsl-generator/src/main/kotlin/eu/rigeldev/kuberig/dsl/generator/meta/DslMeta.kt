package eu.rigeldev.kuberig.dsl.generator.meta

import eu.rigeldev.kuberig.dsl.generator.meta.types.DslTypeMeta

class DslMeta(val platformSpecifics: DslPlatformSpecifics) {
    val typeMeta = mutableMapOf<String, DslTypeMeta>()

    val kindMeta = mutableListOf<DslKindMeta>()

    fun registerType(typeMeta : DslTypeMeta) {
        this.typeMeta[typeMeta.absoluteName] = typeMeta
    }

    fun registerKind(kindMeta : DslKindMeta) {
        this.kindMeta.add(kindMeta)
    }
}