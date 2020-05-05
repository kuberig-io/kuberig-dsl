package eu.rigeldev.kuberig.dsl.generator.gradle

import java.util.*

/**
 * KubeRig DSL plugin properties.
 */
class KubeRigDslProperties(val kubeRigDslVersion: String, val jacksonVersion: String) {

    companion object {
        /**
         * Loads the kuberig-dsl.properties file packaged in the plugin jar.
         * Verifies the property values are sem version (contain 3 parts separated by periods).
         */
        fun load(): KubeRigDslProperties {
            val props = loadProps()

            val kubeRigDslVersion = props["kuberig.dsl.version"] as String
            val jacksonVersion = props["jackson.version"] as String

            return load(kubeRigDslVersion, jacksonVersion)
        }

        fun load(kubeRigDslVersion: String, jacksonVersion: String): KubeRigDslProperties {

            check(kubeRigDslVersion != "'$'{kuberigDslVersion}") { "kuberig.dsl.version ${kubeRigDslVersion} in kuberig-dsl.properties was not properly replaced during build."}
            check(kubeRigDslVersion != "0.0.0") { "kuberig.dsl.version ${kubeRigDslVersion} in kuberig-dsl.properties was not properly replaced during build."}
            check(kubeRigDslVersion.split('.').size == 3) { "kuberig.dsl.version ${kubeRigDslVersion} in kuberig-dsl.properties is not a valid sem-version."}
            check(jacksonVersion.split('.').size == 3) { "jacksonVersion ${jacksonVersion} in kuberig-dsl.properties is not a valid sem-version."}

            return KubeRigDslProperties(
                kubeRigDslVersion,
                jacksonVersion
            )
        }

        private fun loadProps() : Properties {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/kuberig-dsl.properties"))
            return props
        }
    }
}