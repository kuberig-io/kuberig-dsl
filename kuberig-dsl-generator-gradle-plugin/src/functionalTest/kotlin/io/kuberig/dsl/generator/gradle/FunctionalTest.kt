package io.kuberig.dsl.generator.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

abstract class FunctionalTest {

    private val projectDir = File("build/functionalTest/" + UUID.randomUUID().toString())

    @BeforeTest fun init() {
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("io.kuberig.dsl.generator")
            }
            
            repositories {
                jcenter()
                flatDir {
                    dirs("../../../../kuberig-dsl-base/build/libs","../../../../kuberig-dsl-generator/build/libs")
                }
            }
        """.trimIndent())
    }

    protected fun testFor(platform: String, version: String) {
        val sourceSwaggerFile = File("../openapi-specs/$platform/swagger-$version.json")

        val targetSwaggerFile = File(projectDir, "src/main/resources/swagger.json")
        targetSwaggerFile.absoluteFile.parentFile.mkdirs()

        sourceSwaggerFile.copyTo(targetSwaggerFile)

        val runner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("-Dkotlin.daemon.jvm.options=-Xmx3200m", "build")

        val buildResult = runner.build()

        showTestOutput(buildResult)

        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":build")!!.outcome)
        assertEquals(TaskOutcome.SUCCESS, buildResult.task(":generateDslSource")!!.outcome)
    }

    private fun showTestOutput(result: BuildResult) {
        println(result.output.lines()
            .joinToString("\n") { outputLine ->
                "[test output] $outputLine"
            })
    }
}