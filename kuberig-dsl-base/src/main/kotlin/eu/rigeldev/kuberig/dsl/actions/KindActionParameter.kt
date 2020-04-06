package eu.rigeldev.kuberig.dsl.actions

data class KindActionParameter(
        val name: String,
        val description: String,
        val type: String,
        val uniqueItems: Boolean,
        val required: Boolean
)