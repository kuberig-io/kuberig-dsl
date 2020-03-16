package eu.rigeldev.kuberig.dsl

open class BasicMeta(
    val annotations : Map<String, String>?,
    val labels : Map<String, String>?,
    val name : String?,
    val namespace : String?
)