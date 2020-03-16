/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.rigeldev.kuberig.dsl.generator.meta

import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.Kind
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.KindTypes
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.KindUrl
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslTypeMeta

class DslMeta(val platformSpecifics: DslPlatformSpecifics) {
    val typeMeta = mutableMapOf<String, DslTypeMeta>()

    val kindMeta = mutableListOf<DslKindMeta>()

    lateinit var resourceMetadataType : DslTypeName

    val writeableKindUrls = mutableMapOf<Kind, KindUrl>()
    val writeableKindTypes = mutableMapOf<Kind, KindTypes>()

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

    fun kindType(absoluteName: String): Kind? {
        var kind : Kind? = null

        val kindTypesIterator = this.writeableKindTypes.values.iterator()

        while (kind == null && kindTypesIterator.hasNext()) {
            val kindTypes = kindTypesIterator.next()

            val typesIterator = kindTypes.types.iterator()

            while (kind == null && typesIterator.hasNext()) {
                val type = typesIterator.next()

                if (type == absoluteName) {
                    kind = kindTypes.kind
                }
            }
        }

        return kind
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