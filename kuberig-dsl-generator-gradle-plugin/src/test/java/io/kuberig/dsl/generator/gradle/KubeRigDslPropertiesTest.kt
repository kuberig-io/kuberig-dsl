package io.kuberig.dsl.generator.gradle

import org.junit.jupiter.api.Test

internal class KubeRigDslPropertiesTest {

    /**
     * We have had in the past the 0.0.19 tag that did not have the published version correctly replaced in the
     * published artifact. In this case the plugin adds a dependency to kuberig-dsl-base:0.0.0 which of course does not
     * exist breaking the project the kuberig-dsl-generator-gradle-plugin is applied to.
     */
    @Test
    fun verifyPropertyLoading() {
        val dslProps = KubeRigDslProperties.load()

        check(dslProps.kubeRigDslVersion != "'$'{kuberigDslVersion}") { "kuberig.dsl.version ${dslProps.kubeRigDslVersion} in io.kuberig.dsl.generator.properties was not properly replaced during build."}
    }

}
