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

package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslListAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.attributes.DslMapAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslListDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.collections.DslMapDslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslObjectTypeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslTypeMeta
import eu.rigeldev.kuberig.dsl.generator.output.DslMetaConsumer
import java.io.File

class KotlinDslMetaConsumer(private val sourceOutputDirectory : File) : DslMetaConsumer {

    private lateinit var classWriterProducer : KotlinClassWriterProducer

    private lateinit var dslMeta : DslMeta

    override fun consume(dslMeta: DslMeta) {
        this.dslMeta = dslMeta

        this.classWriterProducer = KotlinClassWriterProducer(sourceOutputDirectory)

        this.prepareCollectionTypeMeta()
        dslMeta.typeMeta.values.forEach(this::generateTypeClass)
        dslMeta.typeMeta.values.forEach(this::generateTypeDslClass)
        dslMeta.kindMeta.forEach{ this.generateKindClass(it, dslMeta) }
        this.generateDslRoots(dslMeta)
        this.generateListDslTypes()
        this.generateMapDslTypes()
    }

    private fun prepareCollectionTypeMeta() {
        for (typeMeta in dslMeta.typeMeta.values) {
            if (typeMeta is DslObjectTypeMeta) {

                typeMeta.attributes.forEach { (attributeName, attributeMeta) ->
                    val plural = this.isCollectionAttributeNamePlural(attributeName)

                    if (attributeMeta is DslListAttributeMeta) {
                        val listDslMeta = DslListDslMeta(
                            typeMeta.typeName,
                            attributeMeta,
                            plural
                        )

                        this.dslMeta.addListDslMeta(typeMeta, attributeMeta, listDslMeta)
                    }
                    else if (attributeMeta is DslMapAttributeMeta) {
                        val mapDslMeta = DslMapDslMeta(
                                typeMeta.typeName,
                            attributeMeta,
                            plural
                        )

                        this.dslMeta.addMapDslMeta(typeMeta, attributeMeta, mapDslMeta)
                    }
                }

            }
        }
    }

    private fun isCollectionAttributeNamePlural(attributeName : String) : Boolean {
        return attributeName.endsWith("s")
                && attributeName != "tls"
    }

    private fun generateTypeClass(typeMeta: DslTypeMeta) {
        KotlinApiTypeGenerator(this.classWriterProducer, this.dslMeta)
            .generateApiType(typeMeta.typeName, typeMeta)
    }

    private fun generateTypeDslClass(typeMeta : DslTypeMeta) {
        KotlinApiTypeDslTypeGenerator(this.dslMeta, this.classWriterProducer, listOf("status"))
                .generateApiTypeDslType(typeMeta.typeName.dslTypeName(), typeMeta)
    }

    private fun generateKindClass(kindMeta : DslKindMeta, dslMeta : DslMeta) {
        val kindDslTypeGenerator = KotlinApiTypeDslTypeGenerator(
            dslMeta,
            this.classWriterProducer,
            listOf("kind", "apiVersion", "status"),
            kindMeta
        )

        val kindTypeName = kindMeta.kindType()
        val packageName = kindTypeName.packageName()
        val absoluteName = packageName + "." + kindMeta.kind + "Dsl"
        val typeMeta = dslMeta.typeMeta[kindMeta.typeName.absoluteName]
            ?: throw IllegalStateException("No type meta for $packageName.${kindMeta.kind}")

        kindDslTypeGenerator.generateApiTypeDslType(
            DslTypeName(absoluteName),
            typeMeta
        )
    }

    private fun generateDslRoots(dslMeta : DslMeta) {
        KotlinDslRootsGenerator(this.classWriterProducer)
            .generateDslRoots(dslMeta)
    }

    private fun generateListDslTypes() {
        val listDslTypeGenerator = KotlinListDslTypeGenerator(this.classWriterProducer)

        this.dslMeta.getListDslTypes().forEach(listDslTypeGenerator::generateListDslType)
    }

    private fun generateMapDslTypes() {
        val mapDslTypeGenerator = KotlinMapDslTypeGenerator(this.classWriterProducer)

        this.dslMeta.getMapDslTypes().forEach(mapDslTypeGenerator::generateMapDslType)
    }
}