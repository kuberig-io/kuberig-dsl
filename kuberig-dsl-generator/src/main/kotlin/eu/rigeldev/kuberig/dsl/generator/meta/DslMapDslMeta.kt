package eu.rigeldev.kuberig.dsl.generator.meta

class DslMapDslMeta(val type : DslTypeName,
                    val meta : DslMapAttributeMeta
) {

    fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_MapDsl")
    }

}