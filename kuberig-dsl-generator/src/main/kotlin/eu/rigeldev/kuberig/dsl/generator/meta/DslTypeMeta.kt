package eu.rigeldev.kuberig.dsl.generator.meta

abstract class DslTypeMeta(
    val absoluteName: String,
    val packageName: String,
    val name: String,
    val description: String,
    val typeDependencies: Set<DslTypeName>
)

class DslObjectTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val attributes: Map<String, DslAttributeMeta>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)

class DslContainerTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val containedType: DslTypeName
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)

class DslSealedTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>,
    val sealedTypes: Map<String, DslTypeName>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)

class DslInterfaceTypeMeta(
    absoluteName: String,
    packageName: String,
    name: String,
    description: String,
    typeDependencies: Set<DslTypeName>
) : DslTypeMeta(absoluteName, packageName, name, description, typeDependencies)