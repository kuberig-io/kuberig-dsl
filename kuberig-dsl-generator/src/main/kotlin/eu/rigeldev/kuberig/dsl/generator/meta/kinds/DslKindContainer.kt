package eu.rigeldev.kuberig.dsl.generator.meta.kinds

class DslKindContainer(val name : String) {
    val subContainers = mutableMapOf<String, DslKindContainer>()
    val kinds = mutableMapOf<String, DslKindMeta>()

    fun add(dslKindMeta: DslKindMeta) {

        val containerNameParts = mutableListOf<String>()
        if (dslKindMeta.group != "") {
            val groupSplits = dslKindMeta.group.split('.')
            containerNameParts.addAll(groupSplits)
        }
        containerNameParts.add(dslKindMeta.version)

        println("Adding kind ${dslKindMeta.kind} to ${containerNameParts}")

        this.add(containerNameParts.iterator(), dslKindMeta)
    }

    private fun add(containerNamePartIterator : Iterator<String>, dslKindMeta : DslKindMeta) {
        if (containerNamePartIterator.hasNext()) {
            val group = containerNamePartIterator.next()

            val groupContainer : DslKindContainer

            if (subContainers.containsKey(group)) {
                groupContainer = subContainers[group]!!
            } else {
                groupContainer = DslKindContainer(group)
                subContainers[group] = groupContainer
            }

            groupContainer.add(containerNamePartIterator, dslKindMeta)
        }
        else {
            kinds[dslKindMeta.kind] = dslKindMeta
        }
    }

    fun typeName() : String {
        return "Dsl${name.capitalize()}Root"
    }

}