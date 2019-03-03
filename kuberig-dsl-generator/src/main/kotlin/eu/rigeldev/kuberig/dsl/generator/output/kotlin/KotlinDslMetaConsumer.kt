package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName
import eu.rigeldev.kuberig.dsl.generator.meta.kinds.DslKindMeta
import eu.rigeldev.kuberig.dsl.generator.meta.types.DslTypeMeta
import eu.rigeldev.kuberig.dsl.generator.output.DslMetaConsumer
import java.io.File

class KotlinDslMetaConsumer(private val sourceOutputDirectory : File) : DslMetaConsumer {

    private lateinit var classWriterProducer : KotlinClassWriterProducer

    private lateinit var dslMeta : DslMeta

    override fun consume(dslMeta: DslMeta) {
        this.dslMeta = dslMeta

        this.classWriterProducer = KotlinClassWriterProducer(sourceOutputDirectory)

        dslMeta.typeMeta.values.forEach(this::generateTypeClass)
        dslMeta.typeMeta.values.forEach(this::generateTypeDslClass)
        dslMeta.kindMeta.forEach{ this.generateKindClass(it, dslMeta) }
        this.generateDslRoots(dslMeta)
        this.generateListDslTypes()
        this.generateMapDslTypes()
    }

    private fun generateListDslTypes() {
        val listDslTypeGenerator = KotlinListDslTypeGenerator(this.classWriterProducer)

        this.dslMeta.listDslTypes.forEach(listDslTypeGenerator::generateListDslType)
    }

    private fun generateMapDslTypes() {
        val mapDslTypeGenerator = KotlinMapDslTypeGenerator(this.classWriterProducer)

        this.dslMeta.mapDslTypes.forEach(mapDslTypeGenerator::generateMapDslType)
    }

    private fun generateDslRoots(dslMeta : DslMeta) {
        KotlinDslRootsGenerator(this.classWriterProducer)
            .generateDslRoots(dslMeta)
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

    private fun generateTypeDslClass(typeMeta : DslTypeMeta) {
        KotlinApiTypeDslTypeGenerator(this.dslMeta, this.classWriterProducer, listOf("status"))
            .generateApiTypeDslType(DslTypeName(typeMeta.absoluteName + "Dsl"), typeMeta)
    }

    private fun generateTypeClass(typeMeta: DslTypeMeta) {
        KotlinApiTypeGenerator(this.classWriterProducer)
            .generateApiType(DslTypeName(typeMeta.absoluteName), typeMeta)
    }
}