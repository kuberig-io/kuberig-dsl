package eu.rigeldev.kuberig.dsl.model

/**
 * Basic object that does not have metadata.
 *
 * We need this as some objects don't have the metadata field.
 *
 * Based on the following documentation.
 * https://github.com/kubernetes/community/blob/master/contributors/devel/sig-architecture/api-conventions.md#metadata.
 */
open class BasicResource(
        /**
         * A string that identifies the schema this object should have.
         */
        val kind: String?,
        /**
         * A string that identifies the version of the schema the object should have.
         */
        val apiVersion: String?
)