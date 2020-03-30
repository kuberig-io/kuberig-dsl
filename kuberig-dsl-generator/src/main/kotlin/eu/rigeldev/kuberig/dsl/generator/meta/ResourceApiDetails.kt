package eu.rigeldev.kuberig.dsl.generator.meta

import eu.rigeldev.kuberig.dsl.generator.meta.kinds.Kind
import eu.rigeldev.kuberig.dsl.model.BasicResource

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
        val requestBodyType: DslTypeName?,
        val responseBodyType: DslTypeName?,
        val queryParameters : Map<String, ResourceApiActionParameter>,
        val pathParameters: Map<String, ResourceApiActionParameter>
) {

}

data class ResourceApiActionParameter(
        val name: String,
        val description: String,
        val type: DslTypeName,
        val uniqueItems: Boolean,
        val required: Boolean
)

fun toDslCode(action: ResourceApiAction): String {
        return """
                        ResourceApiAction(
                                \"${action.urlPattern}\",
                                \"${action.httpMethod}\",
                                \"${action.kubernetesAction}\",
                                \"${action.description}\",
                                \"${action.requestBodyType}\",
                                ${toDslCode(action.queryParameters)},
                                ${toDslCode(action.pathParameters)}
                        )
                """.trimIndent()
}

fun toDslCode(parameterMap: Map<String, ResourceApiActionParameter>): String {
        var code = "Map.of(\n"

        parameterMap.forEach{ (name, parameter) ->
                code += "\t\t\t\tPair(\"$name\",${toDslCode(parameter)})\n"
        }

        code += "\n)\n"

        return code
}

fun toDslCode(parameter: ResourceApiActionParameter): String {
        return """
                        ResourceApiActionParameter(
                                \"${parameter.name}\", 
                                \"${parameter.description}\",
                                \"${parameter.type}\",
                                ${parameter.uniqueItems},
                                ${parameter.required}
                        )
                """.trimIndent()
}