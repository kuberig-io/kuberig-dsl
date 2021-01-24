package io.kuberig.dsl.generator.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KubeRigDslGeneratorPluginTest {

    @Test
    fun `plugin registers generateDslSource task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.kuberig.dsl.generator")

        assertNotNull(project.tasks.getByPath("generateDslSource"))
    }

    @Test
    fun `plugin registers kuberigDsl extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.kuberig.dsl.generator")

        val extension = project.extensions.getByName("kuberigDsl") as KubeRigDslGeneratorExtension

        assertNotNull(extension)

        // default locations
        assertNotNull(extension.swaggerFileLocation)
        assertEquals("src/main/resources/swagger.json", extension.swaggerFileLocation.get())
        assertNotNull(extension.sourceOutputDirectoryLocation)
        assertEquals("build/generated-src/main/kotlin", extension.sourceOutputDirectoryLocation.get())

        // default versions
        assertNotNull(extension.jacksonVersionOrDefault())
        assertNotNull(extension.kubeRigDslVersionOrDefault())

        // version overrides
        val jacksonVersionOverride = "8.8.8"
        val kubeRigDslVersionOverride = "9.9.9"

        extension.jacksonVersion = jacksonVersionOverride
        extension.kubeRigDslVersion = kubeRigDslVersionOverride

        assertEquals(jacksonVersionOverride, extension.jacksonVersionOrDefault())
        assertEquals(kubeRigDslVersionOverride, extension.kubeRigDslVersionOrDefault())
    }
}