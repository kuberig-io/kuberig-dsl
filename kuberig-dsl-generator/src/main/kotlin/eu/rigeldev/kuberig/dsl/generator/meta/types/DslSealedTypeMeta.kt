package eu.rigeldev.kuberig.dsl.generator.meta.types

import eu.rigeldev.kuberig.dsl.generator.meta.DslTypeName

/**
 * API Types with a format like 'int-or-string'.
 *
 * These are modelled as sealed kotlin classes.
 *
 * A custom Jackson Serialiser is generated for these types. To preserve correct YAML/JSON output.
 */
class DslSealedTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val sealedTypes: Map<String, DslTypeName>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)