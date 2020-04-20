package eu.rigeldev.kuberig.dsl.generator.output.kotlin

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

class KotlinFactoryMethodsGenerator(private val classWriterProducer : KotlinClassWriterProducer) {

    private val writers = mutableMapOf<String, KotlinClassWriter>()

    fun addFactoryMethod(methodName: String, typeName: DslTypeName) {
        val packageName = typeName.packageName()

        val writerName = "$packageName.FactoryMethods"

        val writer = if (writers.containsKey(writerName)) {
            writers[writerName]!!
        } else {
            val newWriter = KotlinClassWriter(
                DslTypeName(writerName),
                this.classWriterProducer,
                mode = KotlinClassWriterMode.FILE
            )
            writers[writerName] = newWriter
            newWriter
        }

        writer.fileMethod(
            methodName = methodName,
            methodParameters = listOf(
                Pair("init", "${typeName.typeShortName()}.() -> Unit")
            ),
            methodReturnType = typeName.typeShortName(),
            methodCode = listOf(
                "val gen = ${typeName.typeShortName()}()",
                "gen.init()",
                "return gen"
            ),
            methodTypeDependencies = listOf(
                typeName.absoluteName
            )
        )
    }

    fun writeAll() {
        for (writer in writers) {
            writer.value.close()
        }
    }
}