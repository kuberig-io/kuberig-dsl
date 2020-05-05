package eu.rigeldev.kuberig.dsl.generator.gradle

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

internal class KubeRigDslPropertiesTest {

    /**
     * We have had in the past the 0.0.19 tag that did not have the published version correctly replaced in the
     * published artifact. In this case the plugin adds a dependency to kuberig-dsl-base:0.0.0 which of course does not
     * exist breaking the project the kuberig-dsl-generator-gradle-plugin is applied to.
     */
    @Test
    fun verifyPropertyLoading() {
        val props = KubeRigDslProperties.load()

        assertFalse(props.kubeRigDslVersion.isEmpty())
        assertFalse(props.jacksonVersion.isEmpty())
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
