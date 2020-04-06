package eu.rigeldev.kuberig.dsl.generator.meta

import eu.rigeldev.kuberig.dsl.generator.meta.kinds.Kind

data class KindActions(
        val kind: Kind,
        val actions: List<ResourceApiAction>
)

data class ResourceApiAction(
        /**
         * URL pattern this action for the resource is available on.
         *
         * Can contain a {namespace} marker in case this action applies to a resource that is namespaces.
         * Can contain a {name} marker in case this action is for a single instance of the resource this action applies to.
         */
        val urlPattern: String,
        val httpMethod: String,
        val kubernetesAction: String,
        val description: String,
        val requestBodyType: String?,
        val responseBodyType: String?,
        val queryParameters : Map<String, ResourceApiActionParameter>,
        val pathParameters: Map<String, ResourceApiActionParameter>
)

data class ResourceApiActionParameter(
        val name: String,
        val description: String,
        val type: String,
        val uniqueItems: Boolean,
        val required: Boolean
)