package eu.rigeldev.kuberig.dsl.generator.meta

class DslMeta {
    val typeMeta = mutableMapOf<String, DslTypeMeta>()

    val kindMeta = mutableListOf<DslKindMeta>()

    fun registerType(typeMeta : DslTypeMeta) {
        this.typeMeta[typeMeta.absoluteName] = typeMeta
    }

    fun registerKind(kindMeta : DslKindMeta) {
        this.kindMeta.add(kindMeta)
    }
}