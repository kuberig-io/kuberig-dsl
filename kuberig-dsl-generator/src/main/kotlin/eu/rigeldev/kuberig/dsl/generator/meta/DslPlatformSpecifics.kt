package eu.rigeldev.kuberig.dsl.generator.meta

/**
 * Holds Kubernetes/OpenShift specific details.
 */
open class DslPlatformSpecifics(
    val groupKey : String,
    val versionKey : String,
    val kindKey : String,
    val packageNameStart : String)