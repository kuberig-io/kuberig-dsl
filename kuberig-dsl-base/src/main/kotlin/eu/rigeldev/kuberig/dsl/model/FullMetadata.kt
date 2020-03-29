package eu.rigeldev.kuberig.dsl.model

open class FullMetadata(
        val namespace: String?,
        val name: String?,
        val annotations : Map<String, String>?,
        val labels : Map<String, String>?
)