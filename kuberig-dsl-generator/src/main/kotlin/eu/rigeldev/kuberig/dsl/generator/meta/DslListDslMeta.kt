package eu.rigeldev.kuberig.dsl.generator.meta

class DslListDslMeta(val type : DslTypeName,
                     val meta : DslListAttributeMeta
) {

    fun declarationType() : DslTypeName {
        return DslTypeName(type.absoluteName + "_" + meta.name + "_ListDsl")
    }

}