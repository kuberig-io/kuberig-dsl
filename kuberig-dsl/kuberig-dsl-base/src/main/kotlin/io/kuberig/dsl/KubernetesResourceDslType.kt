package io.kuberig.dsl

import io.kuberig.dsl.model.BasicResource

/**
 * Interface implemented by generated DSL classes that map to a Kubernetes Kind.
 */
interface KubernetesResourceDslType<T : BasicResource> : DslType<T> {
}