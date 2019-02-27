package eu.rigeldev.kuberig.dsl.generator

import eu.rigeldev.kuberig.dsl.generator.input.DslMetaProducer
import eu.rigeldev.kuberig.dsl.generator.input.swagger.SwaggerDslMetaProducer
import eu.rigeldev.kuberig.dsl.generator.output.DslMetaConsumer
import eu.rigeldev.kuberig.dsl.generator.output.kotlin.KotlinDslMetaConsumer
import java.io.*


class DslCodeGenerator(projectRootDirectory: File) {
    /**
     * By convention the Swagger API specification file is expected in the 'src/main/resources' directory.
     * And the filename needs to be 'swagger.json'.
     */
    private val swaggerFile = File(projectRootDirectory, "src/main/resources/swagger.json")
    /**
     *
     */
    private val sourceOutputDirectory = File(projectRootDirectory, "build/generated-src/main/kotlin")

    fun generate() {
        println("processing...")

        // input for dsl meta
        val dslMetaProducer : DslMetaProducer = SwaggerDslMetaProducer(swaggerFile)

        val dslMeta = dslMetaProducer.provide()

        // output from dsl meta

        val kotlinDslMetaConsumer : DslMetaConsumer = KotlinDslMetaConsumer(sourceOutputDirectory)

        kotlinDslMetaConsumer.consume(dslMeta)

        println("processed.")
    }
}

fun main(args : Array<String>) {
    DslCodeGenerator(File(args[0])).generate()
}