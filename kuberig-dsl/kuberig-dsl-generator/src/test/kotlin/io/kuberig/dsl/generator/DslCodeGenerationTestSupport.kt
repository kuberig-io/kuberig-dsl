package io.kuberig.dsl.generator

import java.io.File

class DslCodeGenerationTestSupport(private val projectDir: File) {

    fun generateDslCode(platform: String, version: String) {
        val sourceSwaggerFile = File("../openapi-specs/$platform/swagger-$version.json")

        val targetSwaggerFile = File(projectDir, "src/main/resources/swagger.json")
        targetSwaggerFile.absoluteFile.parentFile.mkdirs()

        sourceSwaggerFile.copyTo(targetSwaggerFile)

        DslCodeGenerator(projectDir)
            .generate()

        // TODO compile output to verify generation produces valid kotlin code.
        // The compile is currently done in the plugin module. But that does not generate coverage statistics so we do the generate here to gather the coverage statistics.
    }
}