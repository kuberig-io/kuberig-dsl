package eu.rigeldev.kuberig.dsl

interface DslResourceSink {

    fun add(resource : DslResource)
}