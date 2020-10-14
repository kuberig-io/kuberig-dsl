package io.kuberig.dsl.generator.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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

        assertNotNull(project.extensions.getByName("kuberigDsl"))
    }
}