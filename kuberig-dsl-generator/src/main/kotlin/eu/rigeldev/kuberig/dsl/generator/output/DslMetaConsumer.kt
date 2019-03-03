package eu.rigeldev.kuberig.dsl.generator.output

import eu.rigeldev.kuberig.dsl.generator.meta.DslMeta

interface DslMetaConsumer {

    fun consume(dslMeta : DslMeta)

}