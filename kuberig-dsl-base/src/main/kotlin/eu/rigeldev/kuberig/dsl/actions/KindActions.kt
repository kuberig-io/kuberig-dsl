package eu.rigeldev.kuberig.dsl.actions

data class KindActions(
        val kindId: KindId,
        val actions: List<KindAction>
)