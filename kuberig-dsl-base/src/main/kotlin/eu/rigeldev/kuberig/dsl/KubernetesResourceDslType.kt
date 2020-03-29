package eu.rigeldev.kuberig.dsl

import eu.rigeldev.kuberig.dsl.model.BasicResource

interface KubernetesResourceDslType<T : BasicResource> : DslType<T> {
}