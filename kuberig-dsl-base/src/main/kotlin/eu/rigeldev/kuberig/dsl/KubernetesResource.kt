package eu.rigeldev.kuberig.dsl

open class KubernetesResource (
    val apiVersion: String?,
    val kind: String?,
    val metadata: BasicMeta?
)