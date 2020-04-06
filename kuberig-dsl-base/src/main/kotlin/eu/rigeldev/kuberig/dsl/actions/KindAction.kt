package eu.rigeldev.kuberig.dsl.actions

data class KindAction(
        val urlPattern: String,
        val httpMethod: String,
        val kubernetesAction: String,
        val description: String,
        val requestBodyType: String?,
        val responseBodyType: String?,
        val queryParameters: Map<String, KindActionParameter>,
        val pathParameters: Map<String, KindActionParameter>
)