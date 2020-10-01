package eu.rigeldev.kuberig.dsl.generator.gradle

import org.gradle.api.Project
import java.io.File
import java.util.*

/**
 * KubeRig DSL plugin properties.
 */
class KubeRigDslProperties(val kubeRigDslVersion: SemVersion, val jacksonVersion: SemVersion) {

    companion object {
        /**
         * Loads the kuberig-dsl.properties file packaged in the plugin jar.
         * Verifies the property values are sem version (contain 3 parts separated by periods).
         */
        fun load(props: Properties): KubeRigDslProperties {
            val kubeRigDslVersion = props["kuberig.dsl.version"] as String
            val jacksonVersion = props["jackson.version"] as String

            return load(kubeRigDslVersion, jacksonVersion)
        }

        fun load(kubeRigDslVersion: String, jacksonVersion: String): KubeRigDslProperties {

            check(kubeRigDslVersion != "'$'{kuberigDslVersion}") { "kuberig.dsl.version $kubeRigDslVersion in kuberig-dsl.properties was not properly replaced during build."}
            check(kubeRigDslVersion != "0.0.0") { "kuberig.dsl.version $kubeRigDslVersion in kuberig-dsl.properties was not properly replaced during build."}

            return KubeRigDslProperties(
                SemVersion.fromVersionText(kubeRigDslVersion),
                SemVersion.fromVersionText(jacksonVersion)
            )
        }
    }
}