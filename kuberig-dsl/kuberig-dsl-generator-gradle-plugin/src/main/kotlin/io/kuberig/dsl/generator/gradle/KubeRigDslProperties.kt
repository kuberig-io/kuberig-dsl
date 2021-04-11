package io.kuberig.dsl.generator.gradle

import java.util.*

/**
 * KubeRig DSL plugin properties.
 */
class KubeRigDslProperties(val kubeRigDslVersion: String, val jacksonVersion: String) {

    companion object {
        private fun loadProps() : Properties {
            val props = Properties()
            props.load(this::class.java.getResourceAsStream("/io.kuberig.dsl.generator.properties"))
            return props
        }

        /**
         * Loads the io.kuberig.dsl.generator.properties file packaged in the plugin jar.
         * Verifies the property values are sem version (contain 3 parts separated by periods).
         */
        fun load(): KubeRigDslProperties {
            val props = loadProps()

            val kubeRigDslVersion = props["kuberig.dsl.version"] as String
            val jacksonVersion = props["jackson.version"] as String

            return KubeRigDslProperties(
                kubeRigDslVersion,
                jacksonVersion
            )
        }
    }
}