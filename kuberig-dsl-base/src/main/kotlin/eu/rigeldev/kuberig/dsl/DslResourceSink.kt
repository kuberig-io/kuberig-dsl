package eu.rigeldev.kuberig.dsl

interface DslResourceSink {

    fun <T> add(resource : DslResource<T>)
}