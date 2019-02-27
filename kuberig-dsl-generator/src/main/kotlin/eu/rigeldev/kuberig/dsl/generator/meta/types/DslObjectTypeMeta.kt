package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslAttributeMeta
import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * API types with attributes.
 */
class DslObjectTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val attributes: Map<String, DslAttributeMeta>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)