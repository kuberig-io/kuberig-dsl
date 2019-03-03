package eu.rigeldev.kuberig.dsl.generator.input

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta

/**
 * Produces DslMeta from some source.
 */
interface DslMetaProducer {

    fun provide() : DslMeta
}