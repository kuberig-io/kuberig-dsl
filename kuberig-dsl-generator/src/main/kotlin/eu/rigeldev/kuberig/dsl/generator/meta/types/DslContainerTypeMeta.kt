package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * API types that are not objects.
 *
 * A container class is generated for these types.
 *
 * A custom Jackson Serialiser is generated for these types. To preserve correct YAML/JSON output.
 */
class DslContainerTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val containedType: DslTypeName
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)