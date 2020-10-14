package io.kuberig.dsl.generator.gradle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

internal class KubeRigDslPropertiesTest {

    /**
     * We have had in the past the 0.0.19 tag that did not have the published version correctly replaced in the
     * published artifact. In this case the plugin adds a dependency to kuberig-dsl-base:0.0.0 which of course does not
     * exist breaking the project the kuberig-dsl-generator-gradle-plugin is applied to.
     */
    @Test
    fun verifyPropertyLoading() {
        val props = Properties()
        props.setProperty("kuberig.dsl.version", "0.0.22")
        props.setProperty("jackson.version", "2.9.8")

        val dslProps = KubeRigDslProperties.load(props)

        assertEquals("0.0.22", dslProps.kubeRigDslVersion.versionText)
        assertEquals("2.9.8", dslProps.jacksonVersion.versionText)
    }

    @Test
    fun fallbackVersion() {
        assertThrows(IllegalStateException::class.java) {
            KubeRigDslProperties.load("0.0.0", "2.9.8")
        }
    }

    @Test
    fun notReplaced() {
        assertThrows(IllegalStateException::class.java) {
            KubeRigDslProperties.load("'$'{kuberigDslVersion}", "2.9.8")
        }
    }

    @Test
    fun dslVersionNotSemVersion() {
        assertThrows(IllegalStateException::class.java) {
            KubeRigDslProperties.load("1.1-alpha", "2.9.8")
        }
    }

    @Test
    fun jacksonVersionNotSemVersion() {
        assertThrows(IllegalStateException::class.java) {
            KubeRigDslProperties.load("0.0.20", "3.0-RC.1")
        }
    }

}
