package io.kuberig.dsl.model

/**
 * Metadata object that includes annotations and labels.
 *
 * Based on the following documentation.
 * https://github.com/kubernetes/community/blob/master/contributors/devel/sig-architecture/api-conventions.md#metadata.
 *
 * According to the documentation there can be objects that have metadata without annotations and labels.
 * We have not run into those so we limit the base model to FullMetadata.
 */
open class FullMetadata(
        /**
         * A namespace is a DNS compatible label that objects are subdivided into.
         * See the namespace docs https://kubernetes.io/docs/user-guide/namespaces/ for more.
         */
        val namespace: String?,
        /**
         * A string that uniquely identifies this object within the current namespace (see the identifiers docs
         * https://kubernetes.io/docs/user-guide/identifiers/ ).
         * This value is used in the path when retrieving an individual object.
         */
        val name: String?,
        /**
         * a map of string keys and values that can be used by external tooling to store and retrieve arbitrary
         * metadata about this object (see the annotations docs https://kubernetes.io/docs/user-guide/annotations/).
         */
        val annotations : Map<String, String>?,
        /**
         * A map of string keys and values that can be used to organize and categorize objects (see the labels docs
         * https://kubernetes.io/docs/user-guide/labels/).
         */
        val labels : Map<String, String>?
)