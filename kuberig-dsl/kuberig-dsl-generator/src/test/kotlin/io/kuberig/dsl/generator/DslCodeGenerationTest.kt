package io.kuberig.dsl.generator

import java.io.File
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.Test

class DslCodeGenerationTest {

    private val projectDir = File("build/test/" + UUID.randomUUID().toString())

    @Test
    fun generationForKubernetes_v1_16_0() {
        DslCodeGenerationTestSupport(projectDir)
            .generateDslCode("kubernetes", "1.16.0")
    }

    @Test
    fun generationForOpenShift_v3_11_0() {
        DslCodeGenerationTestSupport(projectDir)
            .generateDslCode("openshift", "3.11.0")
    }

    @Test
    fun generationForOpenShift_v4_6_0() {
        DslCodeGenerationTestSupport(projectDir)
            .generateDslCode("openshift", "4.6.0")
    }

    @AfterTest
    fun cleanup() {
        projectDir.deleteRecursively()
    }
}