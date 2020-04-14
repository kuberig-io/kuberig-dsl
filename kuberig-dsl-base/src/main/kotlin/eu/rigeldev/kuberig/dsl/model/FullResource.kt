package eu.rigeldev.kuberig.dsl.model

/**
 * Full object that has (full)metadata including annotations and labels.
 *
 * Based on the following documentation.
 * https://github.com/kubernetes/community/blob/master/contributors/devel/sig-architecture/api-conventions.md#metadata.
 */
open class FullResource(
        kind: String?,
        apiVersion: String?,
        val metadata: FullMetadata?
) : BasicResource(kind, apiVersion)