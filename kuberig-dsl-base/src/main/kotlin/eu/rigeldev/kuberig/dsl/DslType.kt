package eu.rigeldev.kuberig.dsl

/**
 * Interface implemented by all generated DSL classes.
 *
 * DSL classes are mutable.
 * This interface only requires a single toValue() method that returns the immutable type holding a copy of
 * the written values.
 *
 */
interface DslType<T> {

    /**
     * Return the immutable type.
     */
    fun toValue() : T
}