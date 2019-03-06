package eu.rigeldev.kuberig.dsl.generator.meta

import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslTypeMeta

class DslMeta(val platformSpecifics: DslPlatformSpecifics) {
    val typeMeta = mutableMapOf<String, DslTypeMeta>()

    val kindMeta = mutableListOf<DslKindMeta>()

    private val listDslTypes = mutableMapOf<String, DslListDslMeta>()
    private val mapDslTypes = mutableMapOf<String, DslMapDslMeta>()

    fun registerType(typeMeta : DslTypeMeta) {
        this.typeMeta[typeMeta.absoluteName] = typeMeta
    }

    fun registerKind(kindMeta : DslKindMeta) {
        this.kindMeta.add(kindMeta)
    }

    fun isPlatformApiType(absoluteName : String) : Boolean {
        return this.platformSpecifics
            .packageNameStarts.stream()
            .anyMatch { packageNameStart -> absoluteName.startsWith(packageNameStart) }
    }

    fun isPlatformApiType(dslTypeName: DslTypeName) : Boolean {
        return this.isPlatformApiType(dslTypeName.absoluteName)
    }

    fun addListDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta, listDslMeta: DslListDslMeta) {
        this.listDslTypes[this.collectionTypeMetaKey(typeMeta, attributeMeta)] = listDslMeta
    }

    fun hasListDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta) : Boolean {
        return this.listDslTypes.containsKey(this.collectionTypeMetaKey(typeMeta, attributeMeta))
    }

    fun getListDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta) : DslListDslMeta {
        return this.listDslTypes[this.collectionTypeMetaKey(typeMeta, attributeMeta)]!!
    }

    fun getListDslTypes(): Collection<DslListDslMeta> {
        return this.listDslTypes.values
    }

    fun addMapDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta, mapDslMeta: DslMapDslMeta) {
        this.mapDslTypes[this.collectionTypeMetaKey(typeMeta, attributeMeta)] = mapDslMeta
    }

    fun hasMapDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta) : Boolean {
        return this.mapDslTypes.containsKey(this.collectionTypeMetaKey(typeMeta, attributeMeta))
    }

    fun getMapDslMeta(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta) : DslMapDslMeta {
        return this.mapDslTypes[this.collectionTypeMetaKey(typeMeta, attributeMeta)]!!
    }

    fun getMapDslTypes() : Collection<DslMapDslMeta> {
        return this.mapDslTypes.values
    }

    private fun collectionTypeMetaKey(typeMeta: DslTypeMeta, attributeMeta: DslAttributeMeta) : String {
        return typeMeta.absoluteName + "_" + attributeMeta.name
    }
}