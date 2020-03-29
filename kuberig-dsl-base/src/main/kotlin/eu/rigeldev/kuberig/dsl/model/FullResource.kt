package eu.rigeldev.kuberig.dsl.model

open class FullResource(
        kind: String?,
        apiVersion: String?,
        val metadata: FullMetadata?
) : BasicResource(kind, apiVersion)