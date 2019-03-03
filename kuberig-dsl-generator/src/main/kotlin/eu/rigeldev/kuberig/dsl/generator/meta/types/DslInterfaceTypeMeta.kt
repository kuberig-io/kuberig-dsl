package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * API Types without attributes.
 *
 * I guess these are special types that will need manual handling (work-in-progress).
 * I need to understand these better.
 */
class DslInterfaceTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)