package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * Base class for understanding/mapping API Types.
 */
abstract class DslTypeMeta(
    val absoluteName: String,
    val packageName: String,
    val name: String,
    val description: String,
    val typeDependencies: Set<DslTypeName>
)
